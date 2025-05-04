#!/bin/bash
# Voyage-Shop SQL Execution Script
# Run SQL files inside Docker MySQL container
# Usage: ./run-sql.sh path/to/your/sqlfile.sql

# Default values
MYSQL_SERVICE="mysql"
MYSQL_USER="root"
MYSQL_PASSWORD="root"
MYSQL_DATABASE="hhplus"
MYSQL_CHARSET="utf8mb4"

# Help function
show_help() {
    echo "Usage: $0 [options] <sql_file_path>"
    echo ""
    echo "Options:"
    echo "  -h, --help                Show this help message"
    echo "  -s, --service NAME        MySQL service name in docker-compose (default: mysql)"
    echo "  -u, --user USERNAME       MySQL username (default: root)"
    echo "  -p, --password PASSWORD   MySQL password (default: root)"
    echo "  -d, --database NAME       MySQL database name (default: hhplus)"
    echo "  --charset CHARSET         MySQL charset (default: utf8mb4)"
    echo ""
    echo "Example:"
    echo "  $0 data/my-script.sql"
    echo "  $0 -d otherdb data/my-script.sql"
    exit 0
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -s|--service)
            MYSQL_SERVICE="$2"
            shift 2
            ;;
        -u|--user)
            MYSQL_USER="$2"
            shift 2
            ;;
        -p|--password)
            MYSQL_PASSWORD="$2"
            shift 2
            ;;
        -d|--database)
            MYSQL_DATABASE="$2"
            shift 2
            ;;
        --charset)
            MYSQL_CHARSET="$2"
            shift 2
            ;;
        -*)
            echo "Unknown option: $1"
            show_help
            ;;
        *)
            SQL_FILE="$1"
            shift
            ;;
    esac
done

# Check if SQL file was provided
if [ -z "$SQL_FILE" ]; then
    echo "Error: No SQL file specified"
    show_help
fi

# Check if file exists
if [ ! -f "$SQL_FILE" ]; then
    echo "Error: SQL file '$SQL_FILE' not found"
    exit 1
fi

echo "Executing SQL file: $SQL_FILE"
echo "Service: $MYSQL_SERVICE"
echo "Database: $MYSQL_DATABASE"

# Execute the SQL file using direct docker compose command
echo "Running SQL command..."
COMPOSE_FILES="-f docker-compose.yml -f docker-compose.app.yml -f docker-compose.monitoring.yml"

# Use direct command to execute
docker compose $COMPOSE_FILES exec -T "$MYSQL_SERVICE" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --default-character-set="$MYSQL_CHARSET" "$MYSQL_DATABASE" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo "SQL execution completed successfully"
else
    echo "Error: SQL execution failed"
    exit 1
fi 