#!/usr/bin/env python3
"""
AI Automation Module
Handles prompt generation and AI CLI interactions (Aider, Claude Code, etc.)
"""

import subprocess
import json
from typing import Dict, Optional, List
from pathlib import Path


class AIAutomation:
    """Handles AI CLI automation for code fixes and false positive detection"""
    
    def __init__(self, 
                 ai_script_path: str = "./scripts/ai_cli_automation_tools/setup/run_ai.sh",
                 ai_tool: str = "aider"):
        """
        Initialize AI automation
        
        Args:
            ai_script_path: Path to the AI shell script
            ai_tool: Which AI tool to use ('aider', 'claude-code', etc.)
        """
        self.ai_script_path = ai_script_path
        self.ai_tool = ai_tool
        
    def generate_fix_prompt(self, alert: Dict) -> str:
        """
        Generate a prompt for fixing security issues
        Works with both Aider and Claude Code
        
        Args:
            alert: GitHub code scanning alert dictionary
            
        Returns:
            Formatted prompt string for AI tool
        """
        rule = alert['rule']
        location = alert['most_recent_instance']['location']
        
        prompt = f"""Please fix this security vulnerability in the code:

**Security Issue Details:**
- Rule ID: {rule['id']}
- Description: {rule.get('full_description', rule.get('description', 'N/A'))}
- Severity: {rule.get('security_severity_level', 'N/A')}
- CWE: {rule.get('tags', [])}

**Exact Location:**
- File: {location['path']}
- Lines: {location.get('start_line', 'N/A')} to {location.get('end_line', 'N/A')}
- Columns: {location.get('start_column', 'N/A')} to {location.get('end_column', 'N/A')}

**Requirements for the Fix:**
1. Completely resolve the security vulnerability
2. Preserve all existing functionality
3. Follow security best practices for this type of issue
4. Add a brief comment explaining the security fix
5. Ensure the fix doesn't introduce new vulnerabilities

**Code Context:**
{alert.get('most_recent_instance', {}).get('message', {}).get('text', 'See the highlighted code section')}
"""
        
        # Add tool-specific and rule-specific guidance
        prompt += self._get_rule_specific_guidance(rule['id'])
        prompt += self._get_tool_specific_instructions()
        
        return prompt
    
    def generate_false_positive_analysis_prompt(self, alert: Dict) -> str:
        """
        Generate a prompt for analyzing if an alert is a false positive
        
        Args:
            alert: GitHub code scanning alert dictionary
            
        Returns:
            Formatted prompt string for false positive analysis
        """
        rule = alert['rule']
        location = alert['most_recent_instance']['location']
        
        prompt = f"""Please analyze this code scanning alert to determine if it's a false positive:

**Alert Information:**
- Rule ID: {rule['id']}
- Description: {rule.get('full_description', rule.get('description', 'N/A'))}
- Severity: {rule.get('security_severity_level', 'N/A')}
- CWE: {rule.get('tags', [])}

**Code Location:**
- File: {location['path']}
- Lines: {location.get('start_line', 'N/A')} to {location.get('end_line', 'N/A')}

**Alert Message:**
{alert.get('most_recent_instance', {}).get('message', {}).get('text', 'No specific message provided')}

**Analysis Required:**
Please examine the code and determine:

1. **Is this a false positive?** (YES/NO)
2. **Reasoning:** Explain why this is or isn't a real security issue
3. **Context Analysis:** 
   - Does the flagged code actually create the vulnerability described?
   - Are there existing mitigations that the scanner missed?
   - Is the data flow analysis correct?
4. **If FALSE POSITIVE, explain:**
   - Why the scanner incorrectly flagged this
   - What context or pattern confused the scanner
   - Whether the code follows secure practices despite the alert
5. **If TRUE POSITIVE, explain:**
   - The actual security risk
   - Potential attack vectors
   - Recommended fix approach

**Response Format:**
Start your response with "FALSE_POSITIVE:" or "TRUE_POSITIVE:" followed by your analysis.

**Important:** Look for common false positive patterns like:
- ORM/DAO usage with parameterized queries that scanner thinks is SQL injection
- Escaped or sanitized input that scanner doesn't recognize
- Security controls in different layers/files
- Test code or mock data
- Already validated input from trusted sources
"""
        
        return prompt
    
    def _get_rule_specific_guidance(self, rule_id: str) -> str:
        """
        Get rule-specific guidance for common vulnerability types
        Works with both Aider and Claude Code
        
        Args:
            rule_id: The rule identifier
            
        Returns:
            Additional guidance string
        """
        guidance_map = {
            "sql-injection": "\n**Specific Fix Guidance:** Use parameterized queries or prepared statements. Never concatenate user input into SQL strings. If using an ORM, ensure you're using the query builder properly.",
            "xss": "\n**Specific Fix Guidance:** Sanitize and escape all user input before rendering. Use context-appropriate encoding (HTML, JavaScript, URL, CSS). Consider using a templating engine with auto-escaping.",
            "path-traversal": "\n**Specific Fix Guidance:** Validate and sanitize file paths. Use allowlists for permitted directories. Resolve canonical paths and ensure they're within expected boundaries.",
            "hardcoded-credentials": "\n**Specific Fix Guidance:** Move credentials to environment variables or secure configuration management. Never commit secrets to version control.",
            "weak-cryptography": "\n**Specific Fix Guidance:** Use strong, modern cryptographic algorithms (AES-256, SHA-256+). Ensure proper key management and avoid deprecated algorithms.",
            "command-injection": "\n**Specific Fix Guidance:** Avoid shell execution with user input. If necessary, use parameterized commands or strict input validation with allowlists.",
            "xxe": "\n**Specific Fix Guidance:** Disable external entity processing in XML parsers. Configure parsers to prevent XXE attacks.",
            "insecure-deserialization": "\n**Specific Fix Guidance:** Avoid deserializing untrusted data. If necessary, use safe serialization formats like JSON and validate input structure.",
        }
        
        for key, guidance in guidance_map.items():
            if key in rule_id.lower():
                return guidance
        return "\n**Note:** Ensure the fix addresses the root cause, not just the symptom."
    
    def _get_tool_specific_instructions(self) -> str:
        """
        Get tool-specific instructions for Aider, Claude Code, etc.
        
        Returns:
            Tool-specific instruction string
        """
        if self.ai_tool == "claude-code":
            return "\n**Claude Code Instructions:** Make the minimal necessary changes to fix the security issue. Preserve code style and formatting."
        elif self.ai_tool == "aider":
            return "\n**Aider Instructions:** Apply the fix directly to the file. Ensure the change is atomic and focused on the security issue."
        else:
            return "\n**AI Tool Instructions:** Fix the security issue with minimal, focused changes."
    
    def run_ai_tool(self, file_path: str, prompt: str, auto_commit: bool = False) -> int:
        """
        Execute AI tool (Aider, Claude Code, etc.) with the given file and prompt
        
        Args:
            file_path: Path to the file to fix
            prompt: The prompt to send to the AI tool
            auto_commit: Whether to auto-commit changes
            
        Returns:
            Process return code
        """
        # Validate file exists
        if not Path(file_path).exists():
            print(f"Warning: File {file_path} does not exist")
            return 1
        
        # Prepare command based on tool
        cmd = [self.ai_script_path, self.ai_tool, file_path]
        if auto_commit:
            cmd.append("--auto-commit")
        
        # Run AI tool
        process = subprocess.Popen(
            cmd,
            stdin=subprocess.PIPE,
            stdout=None,  # Let output go to terminal
            stderr=None,  # Let errors go to terminal
            text=True,
            bufsize=1
        )
        
        # Send prompt line by line
        for line in prompt.splitlines():
            process.stdin.write(line + "\n")
            process.stdin.flush()
        
        process.stdin.close()
        return_code = process.wait()
        
        return return_code
    
    def analyze_false_positives(self, 
                               alerts: List[Dict], 
                               output_file: Optional[str] = None) -> Dict[str, List]:
        """
        Analyze alerts for false positives
        
        Args:
            alerts: List of alert dictionaries
            output_file: Optional JSON file to save results
            
        Returns:
            Dictionary with 'false_positives' and 'true_positives' lists
        """
        results = {
            'false_positives': [],
            'true_positives': [],
            'unclear': []
        }
        
        for i, alert in enumerate(alerts, 1):
            path = alert['most_recent_instance']['location']['path']
            rule = alert['rule']['id']
            
            print(f"\n[{i}/{len(alerts)}] Analyzing {rule} in {path}...")
            
            prompt = self.generate_false_positive_analysis_prompt(alert)
            
            # Here you would normally call the AI tool and parse response
            # For now, this is a placeholder for the analysis logic
            print(f"Generated analysis prompt for {rule}")
            
            # In production, you'd parse the AI response to categorize
            # For demonstration, we'll just add to unclear
            results['unclear'].append({
                'alert': alert,
                'analysis': 'Requires manual review'
            })
        
        # Save results if output file specified
        if output_file:
            with open(output_file, 'w') as f:
                json.dump(results, f, indent=2)
            print(f"\nAnalysis results saved to {output_file}")
        
        return results
    
    def batch_process_alerts(self, 
                           alerts: List[Dict],
                           mode: str = "fix",
                           skip_extensions: Optional[List[str]] = None,
                           dry_run: bool = False) -> Dict[str, List]:
        """
        Process multiple alerts in batch for fixes or false positive analysis
        
        Args:
            alerts: List of alert dictionaries
            mode: Processing mode ('fix' or 'analyze')
            skip_extensions: File extensions to skip (e.g., ['.js', '.css'])
            dry_run: If True, only generate prompts without running AI tool
            
        Returns:
            Dictionary with processing results
        """
        skip_extensions = skip_extensions or []
        results = {
            'processed': [],
            'skipped': [],
            'failed': []
        }
        
        for i, alert in enumerate(alerts, 1):
            path = alert['most_recent_instance']['location']['path']
            rule = alert['rule']['id']
            
            # Check if should skip
            if any(path.endswith(ext) for ext in skip_extensions):
                print(f"[{i}/{len(alerts)}] Skipping {path} (excluded extension)")
                results['skipped'].append(alert)
                continue
            
            # Generate appropriate prompt based on mode
            if mode == "fix":
                prompt = self.generate_fix_prompt(alert)
                action = "Fixing"
            else:
                prompt = self.generate_false_positive_analysis_prompt(alert)
                action = "Analyzing"
            
            if dry_run:
                print(f"\n[{i}/{len(alerts)}] DRY RUN - {action} {rule} in {path}")
                print("=" * 50)
                print(prompt[:500] + "..." if len(prompt) > 500 else prompt)
                results['processed'].append(alert)
            else:
                print(f"\n[{i}/{len(alerts)}] {action} {rule} in {path}...")
                return_code = self.run_ai_tool(path, prompt)
                
                if return_code == 0:
                    results['processed'].append(alert)
                    print(f"✓ Successfully processed")
                else:
                    results['failed'].append(alert)
                    print(f"✗ Failed to process")
        
        return results