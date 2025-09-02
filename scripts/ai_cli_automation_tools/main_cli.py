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
    """Setup command line argument parser"""
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
        choices=["aider", "claude-code", "custom"],
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
        "--skip-extensions",
        nargs="+",
        default=[".js"],
        help="File extensions to skip (e.g., .js .css)"
    )
    
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Only show what would be done without making changes"
    )
    
    parser.add_argument(
        "--interactive",
        action="store_true",
        help="Run in interactive mode with prompts"
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
    
    parser.add_argument(
        "--auto-dismiss",
        action="store_true",
        help="Automatically dismiss false positives (analyze mode only)"
    )
    
    return parser


def interactive_mode():
    """Run in interactive mode with user prompts"""
    print("=" * 60)
    print("GitHub Code Scanning Alert Management")
    print("Supports: Aider, Claude Code, and other AI tools")
    print("=" * 60)
    
    # Get mode first
    print("\nWhat would you like to do?")
    print("1. Fix security issues")
    print("2. Analyze for false positives")
    mode_choice = input("Enter choice (1 or 2): ").strip()
    mode = "fix" if mode_choice == "1" else "analyze"
    
    # Get AI tool preference
    print("\nWhich AI tool are you using?")
    print("1. Aider")
    print("2. Claude Code")
    print("3. Custom/Other")
    tool_choice = input("Enter choice (1-3): ").strip()
    ai_tool = ["aider", "claude-code", "custom"][int(tool_choice) - 1] if tool_choice.isdigit() else "aider"
    
    # Get repository information
    owner = input("\nEnter repository owner [openo-beta]: ").strip() or "openo-beta"
    repo = input("Enter repository name [Open-O]: ").strip() or "Open-O"
    
    # Get filter preferences
    print("\n--- Filter Options ---")
    rule_filter = input("Enter rule ID to filter (or press Enter for all): ").strip() or None
    
    severity_filter = None
    if input("Filter by severity? (y/n): ").strip().lower() == 'y':
        severity_filter = input("Enter severity (critical/high/medium/low): ").strip().lower()
    
    # Initialize clients
    print(f"\nInitializing {ai_tool} automation...")
    github_client = GitHubCodeScanning(owner, repo)
    ai_client = AIAutomation(ai_tool=ai_tool)
    
    # Fetch alerts
    print("\nFetching alerts...")
    alerts = github_client.fetch_alerts(
        rule_filter=rule_filter,
        severity_filter=severity_filter
    )
    
    if not alerts:
        print("❌ No matching alerts found.")
        return
    
    print(f"✓ Found {len(alerts)} matching alerts.")
    
    # Mode-specific processing
    if mode == "fix":
        print("\n--- Fix Configuration ---")
        skip_js = input("Skip JavaScript files? (y/n) [y]: ").strip().lower() != 'n'
        skip_extensions = [".js"] if skip_js else []
        
        dry_run = input("Perform dry run only? (y/n) [n]: ").strip().lower() == 'y'
        
        confirm_each = input("Confirm before each fix? (y/n) [n]: ").strip().lower() == 'y'
        
        # Process alerts for fixing
        if confirm_each:
            for i, alert in enumerate(alerts, 1):
                path = alert['most_recent_instance']['location']['path']
                rule = alert['rule']['id']
                severity = alert['rule'].get('security_severity_level', 'unknown')
                
                print(f"\n[{i}/{len(alerts)}] Alert: {rule}")
                print(f"  File: {path}")
                print(f"  Severity: {severity}")
                
                proceed = input("Fix this issue? (y/n/q to quit) [y]: ").strip().lower()
                
                if proceed == 'q':
                    break
                elif proceed != 'n':
                    prompt = ai_client.generate_fix_prompt(alert)
                    if not dry_run:
                        ai_client.run_ai_tool(path, prompt)
                    else:
                        print("\nDRY RUN - Generated prompt:")
                        print(prompt[:500] + "..." if len(prompt) > 500 else prompt)
        else:
            results = ai_client.batch_process_alerts(
                alerts,
                mode="fix",
                skip_extensions=skip_extensions,
                dry_run=dry_run
            )
            print_summary(results, mode="fix")
    
    else:  # analyze mode
        print("\n--- False Positive Analysis Configuration ---")
        output_file = input("Save results to file? Enter filename (or press Enter to skip): ").strip()
        
        auto_dismiss = input("Auto-dismiss confirmed false positives? (y/n) [n]: ").strip().lower() == 'y'
        
        # Analyze alerts
        print("\nAnalyzing alerts for false positives...")
        results = ai_client.analyze_false_positives(alerts, output_file)
        
        print_summary(results, mode="analyze")
        
        if auto_dismiss and results['false_positives']:
            print(f"\n{len(results['false_positives'])} false positives identified.")
            if input("Dismiss these alerts on GitHub? (y/n): ").strip().lower() == 'y':
                for item in results['false_positives']:
                    alert = item['alert']
                    success = github_client.dismiss_alert(
                        alert['number'],
                        'false_positive',
                        f"Identified as false positive by {ai_tool} analysis"
                    )
                    if success:
                        print(f"✓ Dismissed alert #{alert['number']}")
                    else:
                        print(f"✗ Failed to dismiss alert #{alert['number']}")


def print_summary(results: dict, mode: str = "fix"):
    """Print processing summary"""
    print("\n" + "=" * 60)
    print(f"{'Fix' if mode == 'fix' else 'Analysis'} Summary")
    print("=" * 60)
    
    if mode == "fix":
        print(f"✓ Processed: {len(results.get('processed', []))}")
        print(f"⊘ Skipped: {len(results.get('skipped', []))}")
        print(f"✗ Failed: {len(results.get('failed', []))}")
        
        if results.get('failed'):
            print("\nFailed alerts:")
            for alert in results['failed']:
                print(f"  - {alert['rule']['id']}: {alert['most_recent_instance']['location']['path']}")
    else:
        print(f"False Positives: {len(results.get('false_positives', []))}")
        print(f"True Positives: {len(results.get('true_positives', []))}")
        print(f"Unclear/Needs Review: {len(results.get('unclear', []))}")


def main():
    """Main entry point"""
    parser = setup_argparse()
    args = parser.parse_args()
    
    if args.interactive:
        interactive_mode()
        return
    
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
            results = ai_client.batch_process_alerts(
                alerts,
                mode="fix",
                skip_extensions=args.skip_extensions,
                dry_run=args.dry_run
            )
            print_summary(results, mode="fix")
            
        else:  # analyze mode
            results = ai_client.analyze_false_positives(
                alerts,
                output_file=args.output
            )
            print_summary(results, mode="analyze")
            
            # Auto-dismiss if requested
            if args.auto_dismiss and results['false_positives']:
                print(f"\nAuto-dismissing {len(results['false_positives'])} false positives...")
                for item in results['false_positives']:
                    alert = item['alert']
                    success = github_client.dismiss_alert(
                        alert['number'],
                        'false_positive',
                        f"Identified as false positive by {args.ai_tool} analysis"
                    )
                    if success:
                        print(f"✓ Dismissed alert #{alert['number']}")
        
        # Save results if output specified
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