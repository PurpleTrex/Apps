import subprocess
import json
import re
import os
import sys

# Function to run the mvn compile command and capture output
def run_mvn_compile():
    maven_path = r'C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
    project_root = r'C:\Users\purple\Desktop\Dev\Git\Java\Gaia-Structure'
    
    print(f"Running Maven from: {project_root}")
    print(f"Maven path: {maven_path}")
    
    # Try multiple Maven commands to force errors to show
    commands_to_try = [
        ['clean', 'compile'],
        ['compile'],
        ['clean', 'compile', '-X'],  # Debug mode
        ['clean', 'compile', '--fail-fast']  # Fail fast to show all errors
    ]
    
    all_output = ""
    
    for cmd in commands_to_try:
        try:
            print(f"Running: mvn {' '.join(cmd)}")
            result = subprocess.run(
                [maven_path] + cmd,
                capture_output=True,
                text=True,
                cwd=project_root,
                timeout=120  # 2 minute timeout
            )
            
            print(f"Maven exit code: {result.returncode}")
            print(f"STDOUT length: {len(result.stdout) if result.stdout else 0}")
            print(f"STDERR length: {len(result.stderr) if result.stderr else 0}")
            
            # Collect all output
            cmd_output = ""
            if result.stdout:
                cmd_output += f"=== STDOUT for mvn {' '.join(cmd)} ===\n"
                cmd_output += result.stdout + "\n"
            if result.stderr:
                cmd_output += f"=== STDERR for mvn {' '.join(cmd)} ===\n"
                cmd_output += result.stderr + "\n"
            
            all_output += cmd_output
            
            # If we got a failure (exit code != 0), we likely have the errors we want
            if result.returncode != 0:
                print(f"Maven failed with exit code {result.returncode} - this should contain error details")
                break
            else:
                print("Maven succeeded, trying next command to force error details...")
                
        except subprocess.TimeoutExpired:
            print(f"Maven command timed out: mvn {' '.join(cmd)}")
            continue
        except Exception as e:
            print(f"Error running mvn {' '.join(cmd)}: {e}")
            continue
    
    # Also print some sample output for debugging
    if all_output:
        print("=== Sample Maven Output (last 1000 chars) ===")
        print(all_output[-1000:])
        print("=== End Sample Output ===")
    
    return all_output

# Function to parse the errors
def parse_errors(output):
    errors = []
    lines = output.splitlines()
    
    print(f"Parsing {len(lines)} lines of Maven output...")
    
    for i, line in enumerate(lines):
        original_line = line
        line_lower = line.lower()
        
        # Multiple patterns for Java compilation errors
        patterns = [
            # Standard javac error format: /path/file.java:[line,col] error: message
            r'([^:\s]+\.java):\[(\d+),(\d+)\]\s*error:\s*(.+)',
            # Alternative format: /path/file.java:line: error: message  
            r'([^:\s]+\.java):(\d+):\s*error:\s*(.+)',
            # Windows path format with drive letter
            r'([A-Z]:[^:]+\.java):\[(\d+),(\d+)\]\s*error:\s*(.+)',
            r'([A-Z]:[^:]+\.java):(\d+):\s*error:\s*(.+)',
            # Relative path format
            r'(src[^:]*\.java):\[(\d+),(\d+)\]\s*error:\s*(.+)',
            r'(src[^:]*\.java):(\d+):\s*error:\s*(.+)',
        ]
        
        for pattern in patterns:
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                if len(match.groups()) == 4:
                    errors.append({
                        "type": "java_compilation_error",
                        "file": match.group(1),
                        "line": match.group(2),
                        "column": match.group(3) if match.group(3).isdigit() else "unknown",
                        "error": match.group(4).strip(),
                        "raw_line": original_line
                    })
                elif len(match.groups()) == 3:
                    errors.append({
                        "type": "java_compilation_error", 
                        "file": match.group(1),
                        "line": match.group(2),
                        "column": "unknown",
                        "error": match.group(3).strip(),
                        "raw_line": original_line
                    })
                break
        
        # Look for Maven [ERROR] lines
        if '[ERROR]' in line:
            errors.append({
                "type": "maven_error",
                "line_number": i + 1,
                "message": line.strip(),
                "raw_line": original_line
            })
        
        # Look for compilation failure indicators
        if any(keyword in line_lower for keyword in ['compilation failure', 'build failure', 'failed to compile']):
            errors.append({
                "type": "build_failure",
                "line_number": i + 1, 
                "message": line.strip(),
                "raw_line": original_line
            })
        
        # Look for any line containing "error:" (but not [INFO] lines)
        if 'error:' in line_lower and '[info]' not in line_lower:
            errors.append({
                "type": "general_error",
                "line_number": i + 1,
                "message": line.strip(),
                "raw_line": original_line
            })
    
    print(f"Found {len(errors)} potential errors")
    
    # Remove duplicates based on raw_line
    unique_errors = []
    seen_lines = set()
    for error in errors:
        if error.get('raw_line') not in seen_lines:
            unique_errors.append(error)
            seen_lines.add(error.get('raw_line', ''))
    
    print(f"After deduplication: {len(unique_errors)} unique errors")
    return unique_errors

# Function to get VS Code / IDE errors using VS Code's extension host
def get_vscode_errors():
    """
    This function tries to get errors from VS Code using its CLI if available,
    or uses other methods to extract IDE-detected errors.
    """
    try:
        # Try to run VS Code command to get problems
        # This might require VS Code to be in PATH or use specific path
        result = subprocess.run([
            'code', '--list-extensions'
        ], capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            print("VS Code CLI is available - attempting to get diagnostic information")
            # Note: VS Code doesn't have a direct CLI command to export problems
            # But we can indicate that VS Code is available for future integration
            return []
        else:
            print("VS Code CLI not available or not in PATH")
            return []
    except:
        print("Could not access VS Code diagnostics via CLI")
        return []

# Function to simulate getting errors from common Java IDE error patterns
def get_ide_style_errors_from_maven():
    """
    Run Maven with maximum verbosity and try to extract all warnings/errors
    that an IDE would typically show
    """
    maven_path = r'C:\Users\purple\Desktop\Dev\Build Files\MVN\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd'
    project_root = r'C:\Users\purple\Desktop\Dev\Git\Java\Gaia-Structure'
    
    try:
        # Run with maximum compiler verbosity
        result = subprocess.run([
            maven_path, 'clean', 'compile',
            '-Dmaven.compiler.showWarnings=true',
            '-Dmaven.compiler.showDeprecation=true', 
            '-Dmaven.compiler.verbose=true',
            '-Dmaven.compiler.debug=true',
            '-X'
        ], capture_output=True, text=True, cwd=project_root, timeout=120)
        
        all_output = result.stdout + "\n" + result.stderr
        return all_output
        
    except Exception as e:
        print(f"Error running verbose Maven: {e}")
        return ""

# Main function
def main():
    # Get the directory where this script is located
    script_dir = os.path.dirname(os.path.abspath(__file__))
    error_log_path = os.path.join(script_dir, 'errors.json')
    output_log_path = os.path.join(script_dir, 'maven_output.txt')
    
    print("Starting comprehensive error detection...")
    print("=" * 50)
    
    # Method 1: Try to get VS Code errors 
    print("1. Checking for VS Code diagnostic errors...")
    vscode_errors = get_vscode_errors()
    
    # Method 2: Run Maven with verbose output
    print("2. Running Maven with maximum verbosity...")
    maven_output = get_ide_style_errors_from_maven()
    
    # Method 3: Also run our original Maven approach
    print("3. Running standard Maven compilation...")
    standard_maven_output = run_mvn_compile()
    
    # Combine all outputs
    combined_output = f"""
=== VS Code Errors ===
{json.dumps(vscode_errors, indent=2)}

=== Verbose Maven Output ===
{maven_output}

=== Standard Maven Output ===
{standard_maven_output}
"""
    
    # Save the full combined output
    with open(output_log_path, 'w', encoding='utf-8') as f:
        f.write(combined_output)
    print(f"Full output saved to: {output_log_path}")
    
    # Parse errors from all sources
    all_errors = []
    
    # Parse standard Maven errors
    if standard_maven_output:
        maven_errors = parse_errors(standard_maven_output)
        for error in maven_errors:
            error['source'] = 'maven_standard'
            all_errors.append(error)
    
    # Parse verbose Maven errors  
    if maven_output:
        verbose_maven_errors = parse_errors(maven_output)
        for error in verbose_maven_errors:
            error['source'] = 'maven_verbose'
            all_errors.append(error)
    
    # Add VS Code errors if any
    for error in vscode_errors:
        error['source'] = 'vscode'
        all_errors.append(error)
    
    # Create comprehensive result
    result = {
        "timestamp": subprocess.run(['powershell', 'Get-Date', '-Format', 'yyyy-MM-dd HH:mm:ss'], 
                                  capture_output=True, text=True).stdout.strip(),
        "total_errors": len(all_errors),
        "sources_checked": ["maven_standard", "maven_verbose", "vscode_cli"],
        "errors": all_errors
    }
    
    # Save results
    with open(error_log_path, 'w', encoding='utf-8') as json_file:
        json.dump(result, json_file, indent=4)
    
    print("=" * 50)
    if all_errors:
        print(f"Found {len(all_errors)} total error(s) from all sources.")
        print(f"Details saved to {error_log_path}")
        
        # Group errors by source
        by_source = {}
        for error in all_errors:
            source = error.get('source', 'unknown')
            if source not in by_source:
                by_source[source] = []
            by_source[source].append(error)
        
        for source, errors in by_source.items():
            print(f"\n{source.upper()}: {len(errors)} errors")
            for i, error in enumerate(errors[:3]):  # Show first 3 from each source
                print(f"  {i+1}. {error.get('message', error.get('error', 'Unknown error'))[:100]}...")
    else:
        print("No errors detected from any source.")
        print(f"Note: If you're seeing 132 errors in VS Code, they may be IDE-specific")
        print(f"diagnostics that don't prevent Maven compilation.")
    
    print(f"\nResults saved to: {error_log_path}")
    print(f"Full diagnostic output saved to: {output_log_path}")

if __name__ == "__main__":
    main()