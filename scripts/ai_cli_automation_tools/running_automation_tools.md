## Steps to running the automation tools

1. Rebuild your docker container in VSCode:
    - Open Command Palette: `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (macOS)
    - Run: 
    `Dev Containers: Rebuild Container`

2. Create a `.env` file in the `setup` folder and add the following variable:  
    ```env
    PERSONAL_ACCESS_TOKEN=your_token_here
    ```
    *(Must be a classic GitHub token with full repo permissions enabled)*

3. To avoid errors and unnecessary checks with `.min.js` files, create or update an `.aiderignore` file (if you are using aider) at the project root and add:  
    ```
    *.min.js
    ```

4. Input the following command in your terminal (`Ctrl + Shift + `\`) if you are using API keys:  
    ```bash
    export ANTHROPIC_API_KEY=<your_api_key_here>
    ```
5. Make the required shell script executable in your terminal

    ```bash
        chmod +x scripts/ai_cli_automation_tools/setup/install_packages.sh
        chmod +x scripts/ai_cli_automation_tools/setup/run_ai.sh
    ```

6. Run the install_packages shell script:

     ```bash
        bash ./scripts/ai_cli_automation_tools/setup/install_packages.sh
    ```

5. Run the Python automation script by entering the command:  
    ```bash
    .venv/bin/python3 scripts/ai_cli_automation_tools/main_cli.py
    ```

    Please see the main_cli.py for paramaters that can be added to the script call, such as "--ai-tool claude-code --mode analyze"

6. Follow the prompts listed and login / input your API key into claude code if you are using that option

**Possible additions that could be added later:**
- Automated branch and pull request creation    
    - not currently added to give more control for ensuring current alerts are resolved without issues
- More sortable types to look for results with