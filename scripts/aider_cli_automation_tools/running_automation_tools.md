1. Rebuild your docker container in VSCode:
    - Open Command Palette: `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (macOS)
    - Run: 
    `Dev Containers: Rebuild Container`

2. Create a `.env` file in the `setup` folder and add the following variable:  
    ```env
    PERSONAL_ACCESS_TOKEN=your_token_here
    ```
    *(Must be a classic GitHub token with full repo permissions enabled)*

3. To avoid errors and unnecessary checks with `.min.js` files, create or update an `.aiderignore` file at the project root and add:  
    ```
    *.min.js
    ```

4. Input the following command in your terminal (`Ctrl + Shift + `\`):  
    ```bash
    export ANTHROPIC_API_KEY=<your_api_key_here>
    ```

5. Run the Python automation script by entering the command:  
    ```bash
    ./scripts/aider_cli_automation_tools/run_automation_tools.sh
    ```