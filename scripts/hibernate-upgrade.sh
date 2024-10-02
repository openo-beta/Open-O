#!/bin/bash

# Use the current working directory as the target directory
target_dir=$(pwd)

# Find all files ending with 'dao.java', 'Dao.java', 'DAO.java', 'daoImpl.java', 'DaoImpl.java', or 'DAOImpl.java' (case insensitive)
find "$target_dir" -type f -iname '*dao.java' -o -iname '*daoimpl.java' | while read -r file; do
    echo "Processing $file..."

    # Run aider in unattended mode with Deepseek, automatically committing changes and resetting chat history
    python -m aider "$file" --yes --deepseek --no-suggest-shell-commands --message "I am upgrading my project to Hibernate 5.6. Please check if there are HQL queries in this file. If there are no queries, do not look for other files, do not make any changes, do not recommend any diff.  If you find HQL, check the HQL query uses positional parameters without numbers.  If the HQL query uses positional parameters without numbers, update them to use the correct positional parameter format (e.g., ?1, ?2). Ensure that the meaning of the query remains unchanged.  Do not add comments.  Do not make changes if not needed." --auto-commits --max-chat-history 0
done
