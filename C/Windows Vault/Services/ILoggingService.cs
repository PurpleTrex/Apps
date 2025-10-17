using System;

namespace WindowsVault.Services
{
    /// <summary>
    /// Interface for logging service.
    /// </summary>
    public interface ILoggingService
    {
        /// <summary>
        /// Logs an informational message.
        /// </summary>
        void LogInformation(string message);

        /// <summary>
        /// Logs a warning message.
        /// </summary>
        void LogWarning(string message);

        /// <summary>
        /// Logs an error message.
        /// </summary>
        void LogError(string message, Exception? exception = null);

        /// <summary>
        /// Logs a debug message.
        /// </summary>
        void LogDebug(string message);
    }
}
