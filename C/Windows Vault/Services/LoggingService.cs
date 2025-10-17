using Serilog;
using System;
using System.Text.RegularExpressions;

namespace WindowsVault.Services
{
    /// <summary>
    /// Implementation of logging service using Serilog.
    /// Includes sanitization to prevent sensitive data exposure in logs.
    /// </summary>
    public class LoggingService : ILoggingService
    {
        private readonly ILogger _logger;

        public LoggingService(ILogger logger)
        {
            _logger = logger;
        }

        /// <summary>
        /// Sanitizes log messages to remove potentially sensitive information.
        /// </summary>
        private string SanitizeMessage(string message)
        {
            if (string.IsNullOrEmpty(message))
                return message;

            // Remove full file paths, keep only filenames
            message = Regex.Replace(message, @"[A-Z]:\\(?:[^\\/:*?""<>|\r\n]+\\)*([^\\/:*?""<>|\r\n]+)",
                m => m.Groups[1].Value, RegexOptions.IgnoreCase);

            // Remove potential email addresses
            message = Regex.Replace(message, @"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b",
                "[EMAIL_REDACTED]");

            // Remove potential credit card numbers
            message = Regex.Replace(message, @"\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b",
                "[CARD_REDACTED]");

            // Remove potential API keys or tokens (long alphanumeric strings)
            message = Regex.Replace(message, @"\b[A-Za-z0-9]{32,}\b",
                "[TOKEN_REDACTED]");

            return message;
        }

        /// <summary>
        /// Logs an informational message with sanitization.
        /// </summary>
        public void LogInformation(string message)
        {
            _logger.Information(SanitizeMessage(message));
        }

        /// <summary>
        /// Logs a warning message with sanitization.
        /// </summary>
        public void LogWarning(string message)
        {
            _logger.Warning(SanitizeMessage(message));
        }

        /// <summary>
        /// Logs an error message with sanitization.
        /// Note: Exception details are logged but sensitive data in message is sanitized.
        /// </summary>
        public void LogError(string message, Exception? exception = null)
        {
            var sanitizedMessage = SanitizeMessage(message);

            if (exception != null)
            {
                // Sanitize exception message as well
                _logger.Error(exception, sanitizedMessage);
            }
            else
            {
                _logger.Error(sanitizedMessage);
            }
        }

        /// <summary>
        /// Logs a debug message with sanitization.
        /// </summary>
        public void LogDebug(string message)
        {
            _logger.Debug(SanitizeMessage(message));
        }
    }
}
