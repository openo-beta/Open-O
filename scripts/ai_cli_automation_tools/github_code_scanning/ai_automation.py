#!/usr/bin/env python3
"""
AI Automation Module
Handles prompt generation and AI CLI interactions (Aider, Claude Code, etc.)
"""

import subprocess
import json
import sys
from typing import Dict, Optional, List
from pathlib import Path


class AIAutomation:
    def __init__(self, ai_script_path: str = "./scripts/ai_cli_automation_tools/setup/run_ai.sh", ai_tool: str = "claude-code"):
        """
        Initialize AI automation
        
        Args:
            ai_script_path: Path to the AI shell script
            ai_tool: Which AI tool to use ('claude-code', 'aider', etc.)
        """
        self.ai_script_path = ai_script_path
        self.ai_tool = ai_tool
        
    def build_prompt(self, alert: Dict, template: str) -> str:
        """
        Build a prompt from the alert data and template
        
        Args:
            alert: A single alert passed in
            template: The prompt template to use

        Returns:
            Fully built string prompt for the AI tool
        """
        rule = alert['rule']
        loc = alert['most_recent_instance']['location']
        msg = alert.get('most_recent_instance', {}).get('message', {}).get('text', '')
        
        return template.format(
            rule_id=rule['id'],
            description=rule.get('full_description', rule.get('description', 'N/A')),
            severity=rule.get('security_severity_level', 'N/A'),
            cwe=rule.get('tags', []),
            path=loc['path'],
            start_line=loc.get('start_line', 'N/A'),
            end_line=loc.get('end_line', 'N/A'),
            start_col=loc.get('start_column', 'N/A'),
            end_col=loc.get('end_column', 'N/A'),
            message=msg or 'See the highlighted code section'
        )
    
    def generate_fix_prompt(self, alert: Dict) -> str:
        """
        Generate a prompt for fixing an alert
        
        Args:
            alerts: A list of alert dictionaries
            
        Returns:
            String prompt to be build with the template builder
        """
        template = """Please fix this security vulnerability:
                    **Issue:** {rule_id} - {description}
                    **Severity:** {severity} | **CWE:** {cwe}
                    **Location:** {path} (lines {start_line}-{end_line})

                    **Requirements:**
                    1. Completely resolve the vulnerability
                    2. Preserve existing functionality
                    3. Follow security best practices

                    **Context:** {message}"""
        
        prompt = self.build_prompt(alert, template)
        return prompt
    
    def generate_analysis_prompt(self, alert: Dict) -> str:
        """
        Generate a prompt for analyzing if an alert is a false positive
        
        Args:
            alerts: A list of alert dictionaries
            
        Returns:
            String prompt to be build with the template builder
        """
        template = """Analyze this alert for false positive:
                    **Alert:** {rule_id} - {description}
                    **Severity:** {severity} | **Location:** {path}:{start_line}-{end_line}
                    **Message:** {message}

                    **Required Analysis:**
                    1. Is this a false positive? (YES/NO)
                    2. Explain reasoning
                    3. Check for: existing mitigations, escaped input, test code, trusted sources

                    **Response Format:** Start with "FALSE POSITIVE:" or "SECURITY ISSUE:" then explain.""" 
        return self.build_prompt(alert, template)

    def execute_ai(self, prompt: str, mode: str) -> str:
        if self.ai_tool == "claude-code" and mode == "analyze":
            # For analysis, use Claude to just analyze, not edit
            cmd = ["claude", "--print", prompt]
        else:
            # For other tools, use the AI script
            cmd = [self.ai_script_path, self.ai_tool, prompt]
            
        # Execute command
        process = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
        )

        ai_output = process.stdout if process.returncode == 0 else process.stderr

        print(f"\n  AI response:")
        print(f"  {ai_output if ai_output else '[No response]'}")

        return ai_output

    def analyze_alerts(self, alerts: List[Dict], output_file: Optional[str] = None) -> Dict[str, List]:
        """
        Analyze alerts for false positives
        
        Args:
            alerts: A list of alert dictionaries
            output_file: Optional JSON file to save results
            
        Returns:
            Dictionary with a list of results
        """
        results = {
            'false_positives': [],
            'security_issues': [],
            'unclear': []
        }

        for i, alert in enumerate(alerts, 1):
            path = alert['most_recent_instance']['location']['path']
            rule = alert['rule']['id']

            prompt = self.generate_analysis_prompt(alert)
            ai_output = self.execute_ai(prompt, mode="analyze")

            response_clean = ai_output.lower().replace('*', '').strip()
            alert_data = {
                'alert': alert,
                'analysis': ai_output
            }

            if response_clean.startswith("false positive:"):
                results['false_positives'].append(alert_data)
                print(f"  → Identified as a FALSE POSITIVE")
            elif response_clean.startswith("security issue:"):
                results['security_issues'].append(alert_data)
                print(f"  → Identified as a SECURITY ISSUE")
            else:
                results['unclear'].append(alert_data)
                print(f"  → UNCLEAR - requires manual review")

            return results

    def fix_alerts(self, alerts: List[Dict]):
        """
        Fix security issues in alerts

        Args:
            alerts: A list of alert dictionaries
        """

        for i, alert in enumerate(alerts, 1):
            path = alert['most_recent_instance']['location']['path']
            rule = alert['rule']['id']

            prompt = self.generate_fix_prompt(alert)
            self.execute_ai(prompt, mode="fix")