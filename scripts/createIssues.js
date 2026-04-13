const fs = require("fs");
const axios = require("axios");

const token = process.env.GITHUB_TOKEN;
const repo = "guilhascorreia24/MonitorSolos";

const issues = JSON.parse(
    fs.readFileSync("data/issues.json", "utf-8")
).issues;

async function createIssue(issue) {
    try {
        const res = await axios.post(
            `https://api.github.com/repos/${repo}/issues`,
            {
                title: issue.title,
                body: issue.body,
                labels: issue.labels,
            },
            {
                headers: {
                    Authorization: `Bearer ${token}`,
                    Accept: "application/vnd.github+json",
                },
            }
        );

        console.log(`✅ Created: ${issue.title}`);
    } catch (err) {
        console.error(`❌ Error: ${issue.title}`);
        console.error(err.response?.data || err.message);
    }
}

async function run() {
    for (const issue of issues) {
        await createIssue(issue);
    }
}

run();