# Open-OSP EMR DEVCONTAINER Environment Setup

This document outlines the steps for setting up your development environment for the Open-OSP EMR project using Docker
and VS-Code.

## Prerequisites

* **Docker Desktop:** Installed and running
* **VS Code:** Installed with the "Dev Containers" extension
    * Install "Dev Containers" Extension: Open VS Code, click on the Extensions icon (four squares in the left sidebar),
      search for "Dev Containers" by Microsoft and click "Install".
* **Git:** Installed

## Optional: If you are developing on windows it's recommended that you work within WSL (Windows Subsystem for Linux)

### Steps to get started within WSL 

1. **Go to the command line and type wsl**
    ```bash 
    wsl
    ```
2. **Navigate to the home directory** 
    ```bash
    cd $HOME
    ```
3. **Create a directory**
    ```bash
    mkdir softsec
    ```
4. **Navigate to the softsec dir**
    ```bash
    cd softsec
    ```

5. **Continue normally from here**

## Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/cc-ar-emr/Open-O.git
   cd open-osp
   ```

2. **Confirm port availability:**
   Before proceeding, ensure that no other processes (Tomcat, MySQL) are using ports 8080 and 3306 on your PC/Macbook.
   Also, ensure that no other docker containers are running on those ports.

3. **Open the project in VS Code:**
    * Open VS Code and navigate to the project directory.
    ```bash
    code ./
    ```
    * VS Code should automatically detect the `.devcontainer` folder and prompt you to "Reopen in Container".
    * Click "Reopen in Container" to start the development environment.
    * *Note - In case "Reopen in Container" option does not work then:*
        * Look on bottom left of VSCode you will find a remote connection icon (green colored). Click on it.
        * It will prompt few option, select "Reopen in Container".

4. Once it Reopen in Container, you need to wait until it finishes the setup process. This process will:
    * Build the Docker images for the Open-OSP application and database.
    * Configure the images and start the containers.
      You will see the extensions in the left sidebar along with the "Workspace" folder.
      You will also see the Java extension starts processing the project.

### Initial Compilation

Let's now compile Oscar.

This might take absolutely forever when doing it for the first time.

Once you've built it for the first time, a subsequent full build should take about 2 minutes on MacOS and about 8
minutes on Windows, as we'll have managed to cache a bunch of Maven compilation artifacts.

   ```zsh
   make install
   ```

Once the compilation is successful, a `target/oscar` directory full of artifacts will be created. This is a so-called "
Exploded WAR".

## Access the application

* Open your web browser and navigate to `http://localhost:8080`.
* You should see the Open-OSP EMR application running.
* Login credentials for local development are: 
    * Username: oscardoc
    * Password: mac2002
    * PIN     : 1117

### Subsequent Compilations

For developers who are not compiling for the first time in the dev container, it is recommended to clean the previous build artifacts before compiling again. This ensures a fresh build environment.

   ```zsh
   make clean
   make install
   ```

### View Oscar error logs

Currently, oscar error logs are sent to console without saving to a log file. If you need to read oscar error logs from dev container, can use this command to see real-time logs.

   ```zsh
   cat /tomcat.pid | xargs -I {} tail -f /proc/{}/fd/1
   ```

## Additional Notes

* The `.devcontainer/development/config/shared/local.env` file contains environment variables that can be customized for
  your development environment.
* You can find more information about the Open-OSP EMR project and its development environment in the project's
  documentation.

## Switching Branches 

* If you're having issues with the containers after changing branches, it may be due to the container configuration being different
  on the new branch from the one you were on. 
* Fix: 
    1. Close Visual Studio Code.
    2. Remove the containers, images, and volumes from docker so that the new containers can be loaded.
    3. Relaunch Visual Studio Code. This will allow the new containers to be built. 
  
## Build Issues 

* Ensure prior to a build with changes or switching branches you run

    ```zsh 
    make clean
    ```
* If the issue still persists remove the .m2 cache and then run the make clean command. 
  
## Files Included in the Dev-Container Environment

* **`.devcontainer/devcontainer.json`:** Defines the configuration for the development environment, including Docker
  images to use, ports to forward, and user settings.
* **`.devcontainer/development/Dockerfile`:** Defines the Docker image for the Open-OSP application.
* **`.devcontainer/db/Dockerfile`:** Defines the Docker image for the Open-OSP database.
* **`.devcontainer/development/setup/setup.sh`:** Automates the setup process for the development environment.
* **`.devcontainer/docker-compose.yml`:** Defines the Docker Compose configuration for the development environment,
  including the services to run and their dependencies.

## Additional Resources

* **Docker Desktop:** https://www.docker.com/products/docker-desktop
* **Dev Containers Extension:** https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers
* **VS Code Developing with Dev Containers:** https://code.visualstudio.com/docs/devcontainers/containers
* **Docker Desktop Guide: Getting Started:** https://docs.docker.com/desktop
* **Docker Compose:** https://docs.docker.com/compose**
* **Dockerfile:** https://docs.docker.com/reference/dockerfile/

## Opening the Dev-Container Environment in VS Code through WSL

* **Note:** Make sure you have the Prerequisites installed and configured. 

### Steps

1. **Go to the command line and type wsl**
    ```bash 
    wsl
    ```
2. **Navigate to the home directory** 
    ```bash
    cd $HOME
    ```
3. **Create a directory**
    ```bash
    mkdir softsec
    ```
4. **Navigate to the softsec dir**
    ```bash
    cd softsec
    ```
5. **Clone the GitHub repo Open-O**
    ```bash
    git clone https://github.com/cc-ar-emr/Open-O.git
    ```
6. **Navigate to the Open-O repo dir**
    ```bash
    cd Open-O
    ```
7. **Start VS Code.**
    ```bash
    code ./
    ```

* This should open the Open-O repo in VS Code and prompt you to reopen the folder in the dev container environment.

### Checksum locks

Whenever you update, add, or remove a library ensure that the changes are reflected in the dependencies-lock.json. 

When updating a library, the integrity value needs to be updated to correspond with the change. 

Keeping these locks in sync ensures reproducible builds and guards against tampered artifacts.
Only update when you trust the signature of the libraries. 
mvn se.vandmo:dependency-lock-maven-plugin:lock

### Debugging in Tomcat. Setup steps and usage

Download Community Server Connectors in extensions.
 Ensure its set to version - v0.26.19

Add new server. 
    use on disk - /usr/local/tomact/

add make2 file in .devcontainer/development/scripts/make2 

paste in.

#!/usr/bin/env sh

command=$1
skip_tests=true

if [ "$command" = "test" ]; then
  skip_tests=false
fi

case $command in
  clean)
    mvn clean
    ;;
  lock)
    mvn se.vandmo:dependency-lock-maven-plugin:lock
    ;;
  *)
    mvn clean -Dmaven.test.skip=$skip_tests prepare-package war:exploded

    # Assuming the pom.xml file is in the current directory
    pom_file="pom.xml"

    # This version number is the deployable war version from here: https://bitbucket.org/MagentaHealth/oscar/src/master/pom.xml
    # <artifactId>oscar</artifactId>
	  # <packaging>war</packaging>
	  # <version>v24.02.0</version>
	  # <name>OpenOSCAR</name>
    # Extract the first occurrence of version from pom.xml
    version=$(grep -oPm 1 '<version>\K[^<]+' "$pom_file")

    # It *should* be possible to deploy that `oscar-0.0.0-SNAPSHOT` directory to Tomcat using the `oscar-0.0.0-SNAPSHOT` name,
    # but unknown bugs prevent the app from starting up if that name is used. :man_shrugging:
    # So, let's rename it to `oscar`. Then, our properties file in the home directory can also be called `oscar.properties`
    # (it's already been symlinked by the setup scripts).
    # We'll also create a symlink so that we can continue to compile into `oscar-0.0.0-SNAPSHOT`,
    # which is where Maven expects to deposit its artifacts.
    snapshot_src_dir='oscar-'$version
    snapshot_dest_dir='oscar'

    # Only make the link if it hasn't already been linked yet
    if [ -d "target/$snapshot_src_dir" ] && [ ! -L "target/$snapshot_src_dir" ]; then
      mv target/$snapshot_src_dir target/$snapshot_dest_dir
      ln -s $snapshot_dest_dir target/$snapshot_src_dir
    fi
    ;;
esac

run the following commands 

chmod +x ./devcontainer/development/scripts/make2 

run the script. 

./devcontainer/development/scripts/make2

if you encounter an error 
"/usr/bin/env: ‘sh\r’: No such file or directory
/usr/bin/env: use -[v]S to pass options in shebang lines" 

run: 
apt-get update && apt-get install -y dos2unix
dos2unix .devcontainer/development/scripts/make2

then re-run the script. 

right click the tomcat server 

"add deployment" 

Click: exploded

Path: /workspace/target/oscar

Click: select exploded deployment

Right click tomcat server

Publish Server 

Run Debug Mode

You should now be able to use breakpoints within vscode! 

## Enjoy developing with Open-OSP!
