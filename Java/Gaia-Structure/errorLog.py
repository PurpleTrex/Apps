import subprocess
import json
import re

# Function to run the mvn compile command and capture output
def run_mvn_compile():
    maven_path = r'C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
    try:
        result = subprocess.run(
            [maven_path, 'compile'],
            capture_output=True,
            text=True,
            check=True
        )
        return result.stderr  # Return only standard error output
    except subprocess.CalledProcessError as e:
        return e.stderr  # Return the error output if the command fails

# Function to parse the errors
def parse_errors(output):
    errors = []
    # Regex to capture the relevant error information
    error_pattern = re.compile(r'^(.*?)($[\d]+,\s*[\d]+$)?\s*:\s*(.*)$', re.MULTILINE)
    
    for line in output.splitlines():
        match = error_pattern.search(line)
        if match:
            file_with_line = match.group(1)
            line_info = match.group(2)
            error_message = match.group(3)

            if line_info:
                line_number = line_info.strip("()").split(',')[0]  # Get the line number
                errors.append({
                    "file": file_with_line.strip(),
                    "line": line_number.strip(),
                    "error": error_message.strip(),
                })
    return errors

# Main function
def main():
    output = run_mvn_compile()
    if output:
        errors = parse_errors(output)
        with open('errors.json', 'w') as json_file:
            json.dump(errors, json_file, indent=4)
        print("Errors captured in errors.json")
    else:
        print("No errors found.")

if __name__ == "__main__":
    main()
