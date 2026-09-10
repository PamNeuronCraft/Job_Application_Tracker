const {setGlobalOptions} = require("firebase-functions");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {GoogleGenerativeAI} = require("@google/generative-ai");
const axios = require("axios");
const cheerio = require("cheerio");

setGlobalOptions({maxInstances: 10});

// Initialize Gemini with server-side secret key
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

/**
 * Helper to safely extract JSON from AI markdown response.
 * @param {string} rawText The raw text returned from Gemini.
 * @return {object|null} The parsed JSON object or null on error.
 */
function parseJsonFromText(rawText) {
  if (!rawText) return null;
  let jsonString = rawText.trim();

  if (jsonString.includes("```json")) {
    const start = jsonString.indexOf("```json") + 7;
    jsonString = jsonString.substring(start).split("```")[0].trim();
  } else if (jsonString.includes("```")) {
    const start = jsonString.indexOf("```") + 3;
    jsonString = jsonString.substring(start).split("```")[0].trim();
  } else if (jsonString.includes("{")) {
    const firstBrace = jsonString.indexOf("{");
    const lastBrace = jsonString.lastIndexOf("}");
    if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
      jsonString = jsonString.substring(firstBrace, lastBrace + 1);
    }
  }

  try {
    return JSON.parse(jsonString);
  } catch (e) {
    console.error("Failed to parse JSON from AI response:", rawText, e);
    return null;
  }
}

/**
 * 1. Cloud Function: Extract Job Application Details from a Posting URL
 */
exports.extractJobFromUrl = onCall(
    {secrets: ["GEMINI_API_KEY"]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "User must be authenticated.");
      }

      const {url} = request.data;
      if (!url) {
        throw new HttpsError("invalid-argument", "URL parameter is required.");
      }

      try {
        // Clean URL regex (matches https?://...)
        const cleanUrlMatch = url.match(/https?:\/\/[^\s]+/);
        const cleanUrl = cleanUrlMatch ? cleanUrlMatch[0] : url;

        // Scrape webpage text
        const pageResponse = await axios.get(cleanUrl, {
          headers: {
            "User-Agent":
              "Mozilla/5.0 (Linux; Android 10; K) " +
              "AppleWebKit/537.36 (KHTML, like Gecko) " +
              "Chrome/114.0.0.0 Mobile Safari/537.36",
          },
          timeout: 15000,
          maxRedirects: 5,
        });

        const $ = cheerio.load(pageResponse.data);
        const pageText = $("body")
            .text()
            .replace(/\s+/g, " ")
            .substring(0, 10000);

        const prompt = `
          Extract job application details from the text below.
          Return ONLY a JSON object with these keys:
          "jobName", "companyName", "description", "compensation".

          For "compensation", extract raw numeric amount and hourly or annual.
          Ignore currency symbols, but keep numeric values.
          If a range is provided, return average as a single number string.
          Do not use abbreviations like "k" (use "100000" not "100k").
          Look for "Salary Range", "Compensation", "Pay", or amounts
          followed by "/yr" or "/hr".
          Format as "50/hr" or "100000/yr".

          If a value is not found, use null.
          The "description" should be a concise summary of the role.

          Text:
          ${pageText}
        `.trim();

        const model = genAI.getGenerativeModel({model: "gemini-3.6-flash"});
        const aiResult = await model.generateContent(prompt);
        const rawText = aiResult.response.text();

        const parsedObj = parseJsonFromText(rawText);
        if (!parsedObj) {
          throw new HttpsError(
              "internal",
              "Failed to parse job details from AI response.",
          );
        }

        return parsedObj;
      } catch (error) {
        console.error("Error in extractJobFromUrl:", error);
        if (error instanceof HttpsError) throw error;
        throw new HttpsError(
            "internal",
            "Failed to extract job details: " + (error.message || error),
        );
      }
    },
);

/**
 * 2. Cloud Function: Extract Application Status Update from an Email
 */
exports.extractStatusUpdateFromEmail = onCall(
    {secrets: ["GEMINI_API_KEY"]},
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "User must be authenticated.");
      }

      const {subject, emailBody} = request.data;
      if (!subject && !emailBody) {
        throw new HttpsError(
            "invalid-argument",
            "Subject or emailBody is required.",
        );
      }

      try {
        const prompt = `
          Analyze the following email subject and body related to a job app.
          Determine if it indicates a status change.

          Return ONLY a JSON object with these keys:
          "companyName": The name of the company.
          "jobTitle": The title of the position (if found).
          "newStatus": One of: "APPLIED", "INTERVIEW", "OFFER", "REJECTED".
          "confidence": A value from 0.0 to 1.0.

          If it's not a clear job status update, return null.

          Subject: ${subject || ""}
          Body: ${emailBody || ""}
        `.trim();

        const model = genAI.getGenerativeModel({model: "gemini-3.6-flash"});
        const aiResult = await model.generateContent(prompt);
        const rawText = aiResult.response.text();

        if (!rawText || rawText.toLowerCase().includes("null")) {
          return null;
        }

        return parseJsonFromText(rawText);
      } catch (error) {
        console.error("Error in extractStatusUpdateFromEmail:", error);
        if (error instanceof HttpsError) throw error;
        throw new HttpsError(
            "internal",
            "Failed to analyze status update: " + (error.message || error),
        );
      }
    },
);
