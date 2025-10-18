using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using ModernWpf.Controls;

namespace WindowsVault.Helpers
{
    public static class ModernDialog
    {
        public static async Task<ContentDialogResult> ShowAsync(
            string title,
            string content,
            string primaryButtonText = "OK",
            string? secondaryButtonText = null,
            string? closeButtonText = null)
        {
            var dialog = new ContentDialog
            {
                Title = title,
                Content = content,
                PrimaryButtonText = primaryButtonText,
                DefaultButton = ContentDialogButton.Primary
            };

            if (!string.IsNullOrEmpty(secondaryButtonText))
            {
                dialog.SecondaryButtonText = secondaryButtonText;
            }

            if (!string.IsNullOrEmpty(closeButtonText))
            {
                dialog.CloseButtonText = closeButtonText;
            }

            return await dialog.ShowAsync();
        }

        public static async Task ShowSuccessAsync(string message, string title = "Success")
        {
            await ShowAsync(title, message, "OK");
        }

        public static async Task ShowErrorAsync(string message, string title = "Error")
        {
            await ShowAsync(title, message, "OK");
        }

        public static async Task ShowInfoAsync(string message, string title = "Information")
        {
            await ShowAsync(title, message, "OK");
        }

        public static async Task<bool> ShowConfirmAsync(
            string message,
            string title = "Confirm",
            string yesText = "Yes",
            string noText = "No")
        {
            var result = await ShowAsync(title, message, yesText, noText);
            return result == ContentDialogResult.Primary;
        }

        public static async Task<ContentDialogResult> ShowDeleteConfirmAsync(
            string itemName,
            string itemType = "item")
        {
            var dialog = new ContentDialog
            {
                Title = $"Delete {itemType}?",
                Content = $"Are you sure you want to delete '{itemName}'? This action cannot be undone.",
                PrimaryButtonText = "Delete",
                CloseButtonText = "Cancel",
                DefaultButton = ContentDialogButton.Close
            };

            return await dialog.ShowAsync();
        }

        public static async Task<ContentDialogResult> ShowChoiceAsync(
            string title,
            string content,
            string primaryButtonText,
            string secondaryButtonText)
        {
            var dialog = new ContentDialog
            {
                Title = title,
                Content = content,
                PrimaryButtonText = primaryButtonText,
                SecondaryButtonText = secondaryButtonText,
                CloseButtonText = "Cancel",
                DefaultButton = ContentDialogButton.Primary
            };

            return await dialog.ShowAsync();
        }

        public static async Task<ContentDialogResult> ShowConfirmationAsync(
            string title,
            string content,
            string confirmText = "Yes",
            string cancelText = "No")
        {
            var dialog = new ContentDialog
            {
                Title = title,
                Content = content,
                PrimaryButtonText = confirmText,
                CloseButtonText = cancelText,
                DefaultButton = ContentDialogButton.Primary
            };

            return await dialog.ShowAsync();
        }
    }
}
