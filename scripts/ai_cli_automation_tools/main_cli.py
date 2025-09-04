#!/usr/bin/env python3
"""
Main CLI for GitHub Code Scanning Alert Management
Supports both remediation and false positive detection using AI tools (Aider, Claude Code, etc.)
"""

import sys
import argparse
import json
from typing import Optional
from datetime import datetime

# Import from the github_code_scanning subfolder
from github_code_scanning.github_api import GitHubCodeScanning
from github_code_scanning.ai_automation import AIAutomation


def setup_argparse() -> argparse.ArgumentParser:
    """Setup the command line argument parser"""
    parser = argparse.ArgumentParser(
        description="GitHub code scanning alert management with AI assistance (Aider, Claude Code, etc.)"
    )
    
    parser.add_argument(
        "--mode",
        choices=["fix", "analyze"],
        default="fix",
        help="Mode: 'fix' to remediate issues, 'analyze' to detect false positives"
    )
    
    parser.add_argument(
        "--ai-tool",
        choices=["claude-code", "aider", "custom"],
        default="aider",
        help="Which AI tool to use"
    )
    
    parser.add_argument(
        "--owner", 
        default="openo-beta",
        help="GitHub repository owner"
    )
    
    parser.add_argument(
        "--repo",
        default="Open-O",
        help="GitHub repository name"
    )
    
    parser.add_argument(
        "--rule",
        help="Filter by specific rule ID"
    )
    
    parser.add_argument(
        "--severity",
        choices=["critical", "high", "medium", "low"],
        help="Filter by severity level"
    )
    
    parser.add_argument(
        "--state",
        default="open",
        choices=["open", "closed", "dismissed", "fixed"],
        help="Alert state to fetch"
    )

    parser.add_argument(
        "--limit",
        type=int,
        help="Limit number of alerts to process"
    )
    
    parser.add_argument(
        "--output",
        help="Output file for results (JSON format)"
    )
    
    return parser

def main():
    """Main method to run the CLI tool"""
    parser = setup_argparse()
    args = parser.parse_args()
    
    try:
        # Initialize clients
        print(f"Initializing {args.ai_tool} for {args.mode} mode...")
        github_client = GitHubCodeScanning(args.owner, args.repo)
        ai_client = AIAutomation(ai_tool=args.ai_tool)
        
        # Fetch alerts
        print(f"Fetching {args.state} alerts from {args.owner}/{args.repo}...")
        alerts = github_client.fetch_alerts(
            state=args.state,
            rule_filter=args.rule,
            severity_filter=args.severity
        )
        
        if not alerts:
            print("No matching alerts found.")
            return
        
        # Apply limit if specified
        if args.limit:
            alerts = alerts[:args.limit]
        
        print(f"Found {len(alerts)} alerts to process.")
        
        # Process based on mode
        if args.mode == "fix":
            ai_client.fix_alerts(
                alerts,
            )
        else:
            results = ai_client.analyze_alerts(
                alerts,
                output_file=args.output
            )
            
            print("\n" + "=" * 60)
            print("Analysis Summary")
            print("=" * 60)
            
            print(f"False Positives: {len(results.get('false_positives', []))}")
            print(f"Security Issues: {len(results.get('security_issues', []))}")
            print(f"Unclear/Needs Review: {len(results.get('unclear', []))}")
            
        # Save results if output is specified
        if args.output:
            with open(args.output, 'w') as f:
                json.dump({
                    'timestamp': datetime.now().isoformat(),
                    'mode': args.mode,
                    'ai_tool': args.ai_tool,
                    'repository': f"{args.owner}/{args.repo}",
                    'results': results
                }, f, indent=2, default=str)
            print(f"\nResults saved to {args.output}")
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()