Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

' Get the directory where this script is located
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)

' Run the batch file silently (window hidden)
WshShell.Run """" & scriptDir & "\run-gaia.bat""", 0, False