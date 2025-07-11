#!/usr/bin/env python3

import requests
from dotenv import load_dotenv
import os
import subprocess
import textwrap
import pty
import sys

# Get owner and repository names
OWNER = "cc-ar-emr"
REPO = "Open-O"

# Load variables from .env file
load_dotenv()

# Get the token from the environment variable
token = os.getenv("PERSONAL_ACCESS_TOKEN")

# Error if personal access token is not found in env
if not token:
    raise ValueError("PERSONAL_ACCESS_TOKEN not found in environment variables")


# Setting url for finding
url = f"https://api.github.com/repos/{OWNER}/{REPO}/code-scanning/alerts"

# Setting headers for code scanning
headers = {
    "Accept": "application/vnd.github+json",
    "Authorization": f"Bearer {token}"
}

alerts = []
page = 1
per_page = 100
state = "open"

while True:
    params = {"per_page": per_page, "page": page, "state": state}
    response = requests.get(url, headers=headers, params=params)
    if response.status_code != 200:
        print(f"Error {response.status_code}: {response.text}")
        break

    page_alerts = response.json()
    if not page_alerts:
        break  # no more pages

    alerts.extend(page_alerts)
    page += 1

issue_type = ""
issue_type = input("Enter the rule id that you want to address: ") 

if response.status_code == 200:
    print(f"Found {len(alerts)} code scanning alerts:")

    for alert in alerts:
        rule_id = alert['rule']['id'] # Security vulnerability
        severity = alert['rule']['security_severity_level']  # critical, high, etc.
        description = alert['rule']['full_description'] # Description of security vulnerability
        state = alert['state'] # Current state of the alert

        path = alert['most_recent_instance']['location']['path'] # File path of alert
        start_line = alert['most_recent_instance']['location']['start_line'] # Start line of alert
        end_line = alert['most_recent_instance']['location']['end_line'] # End line of alert
        start_column = alert['most_recent_instance']['location']['start_column'] # Start column of alert
        end_column = alert['most_recent_instance']['location']['end_column'] # End line of alert

        if issue_type != "" and rule_id == issue_type or issue_type == "":
            # prompt = textwrap.dedent(f"""\
            # Please fix this error listed below on the file added to the chat:
            # Information about the security issue:
            # Rule: {rule_id} | Description: {description} | Severity: {severity} | State: {state}
            # Location of the security issue:
            # File Path: {path} | Start Line: {start_line} | End Line: {end_line} | Start Column: {start_column} | End Column: {end_column}
            # """)

            # prompt = ("Please fix this error listed below on the file added to the chat:"
            #         "Information about the security issue:"
            # )

            prompt = """Please fix this error listed below on the file added to the chat:
            Information about the security issue:
            Rule: {rule_id} | Description: {description} | Severity: {severity} | State: {state}
            Location of the security issue:
            File Path: {path} | Start Line: {start_line} | End Line: {end_line} | Start Column: {start_column} | End Column: {end_column}
            """

            print("PROMPT:")
            print("-------------------------------------------------------------------------------------")
            print(prompt)
            print("-------------------------------------------------------------------------------------")

            print("Running aider with the code scanning results.....")

            process = subprocess.Popen(
                ["./aider_cli_code_scanning/run_aider.sh", path],
                stdin=subprocess.PIPE,
                stdout=None,  # Let output go to terminal
                stderr=None,  # Let errors go to terminal
                text=True,
                bufsize=1 
            )

            for line in prompt.splitlines():
                process.stdin.write(line)
                process.stdin.flush()

            process.stdin.close()
            process.wait()
        elif issue_type != "" and rule_id != issue_type:
            continue
else:
    print(f"Error: {response.status_code}: {response.text}")