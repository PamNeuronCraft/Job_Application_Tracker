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
 * Helper to clean and sanitize input text.
 * @param {string} text Raw text to clean.
 * @return {string} Sanitized string.
 */
function cleanText(text) {
  if (!text) return "";
  return text
      .replace(/<[^>]*>/g, " ")
      .replace(/\s+/g, " ")
      .trim();
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

        const model = genAI.getGenerativeModel({
          model: "gemini-3.6-flash",
          generationConfig: {
            responseMimeType: "application/json",
          },
        });
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
        const cleanSubject = cleanText(subject).substring(0, 500);
        const cleanBody = cleanText(emailBody).substring(0, 5000);

        const prompt = `
          Analyze the email subject and body below regarding a job application.
          Determine if it indicates a job application status update.

          Allowed status values for "newStatus":
          - "APPLIED": Application received / confirmation of submission.
          - "INTERVIEW": Invitation to interview, phone screen, coding assessment, or scheduling.
          - "OFFER": Job offer extended or details on offer package.
          - "NO_OFFER": Application rejected, position filled, or decision not to move forward.

          Return a JSON object with:
          - "isJobStatusUpdate": boolean (true if this is a job application status update, false otherwise)
          - "companyName": string or null (the company name)
          - "jobTitle": string or null (the position/job title if found)
          - "newStatus": string or null (one of "APPLIED", "INTERVIEW", "OFFER", "NO_OFFER")
          - "confidence": number between 0.0 and 1.0 (how confident you are in this classification)

          Subject: ${cleanSubject}
          Body: ${cleanBody}
        `.trim();

        const model = genAI.getGenerativeModel({
          model: "gemini-3.6-flash",
          generationConfig: {
            responseMimeType: "application/json",
          },
        });

        const aiResult = await model.generateContent(prompt);
        const rawText = aiResult.response.text();
        const parsed = parseJsonFromText(rawText);

        if (!parsed || !parsed.isJobStatusUpdate || !parsed.companyName || !parsed.newStatus) {
          return null;
        }

        let status = parsed.newStatus;
        if (status === "REJECTED") {
          status = "NO_OFFER";
        }

        const validStatuses = ["APPLIED", "INTERVIEW", "OFFER", "NO_OFFER"];
        if (!validStatuses.includes(status)) {
          return null;
        }

        return {
          companyName: parsed.companyName,
          jobTitle: parsed.jobTitle || null,
          newStatus: status,
          confidence: typeof parsed.confidence === "number" ? parsed.confidence : 0.5,
        };
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
