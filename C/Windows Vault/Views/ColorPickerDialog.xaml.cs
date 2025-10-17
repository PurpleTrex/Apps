using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Shapes;

namespace WindowsVault.Views
{
    public partial class ColorPickerDialog : Window
    {
        public string SelectedColor { get; set; } = "#3B82F6";

        private static readonly string[] CommonColors = new[]
        {
            // Blues
            "#1E40AF", "#2563EB", "#3B82F6", "#60A5FA", "#93C5FD", "#DBEAFE",
            // Greens
            "#065F46", "#047857", "#10B981", "#34D399", "#6EE7B7", "#D1FAE5",
            // Reds
            "#991B1B", "#DC2626", "#EF4444", "#F87171", "#FCA5A5", "#FEE2E2",
            // Yellows/Oranges
            "#92400E", "#EA580C", "#F59E0B", "#FBBF24", "#FCD34D", "#FEF3C7",
            // Purples
            "#581C87", "#7C3AED", "#8B5CF6", "#A78BFA", "#C4B5FD", "#EDE9FE",
            // Pinks
            "#9F1239", "#DB2777", "#EC4899", "#F472B6", "#F9A8D4", "#FCE7F3",
            // Grays
            "#1F2937", "#374151", "#4B5563", "#6B7280", "#9CA3AF", "#D1D5DB",
            // Teals
            "#134E4A", "#0F766E", "#14B8A6", "#2DD4BF", "#5EEAD4", "#CCFBF1",
            // Indigos
            "#312E81", "#4338CA", "#6366F1", "#818CF8", "#A5B4FC", "#E0E7FF",
            // Lime
            "#365314", "#4D7C0F", "#65A30D", "#84CC16", "#A3E635", "#D9F99D",
            // Cyan
            "#164E63", "#0E7490", "#0891B2", "#06B6D4", "#22D3EE", "#CFFAFE",
            // Rose
            "#881337", "#BE123C", "#E11D48", "#F43F5E", "#FB7185", "#FFE4E6",
        };

        public ColorPickerDialog(string initialColor = "#3B82F6")
        {
            InitializeComponent();
            DataContext = this;
            SelectedColor = initialColor;
            
            LoadColorPalette();
        }

        private void LoadColorPalette()
        {
            foreach (var colorHex in CommonColors)
            {
                var button = new Border
                {
                    Width = 50,
                    Height = 50,
                    Background = new SolidColorBrush((Color)ColorConverter.ConvertFromString(colorHex)),
                    Margin = new Thickness(4),
                    CornerRadius = new CornerRadius(6),
                    BorderBrush = Brushes.Gray,
                    BorderThickness = new Thickness(1),
                    Cursor = Cursors.Hand,
                    ToolTip = colorHex
                };

                button.MouseLeftButtonDown += (s, e) =>
                {
                    SelectedColor = colorHex;
                    HexColorTextBox.Text = colorHex;
                    UpdatePreview();
                };

                // Add hover effect
                button.MouseEnter += (s, e) =>
                {
                    button.BorderBrush = Brushes.White;
                    button.BorderThickness = new Thickness(2);
                };

                button.MouseLeave += (s, e) =>
                {
                    button.BorderBrush = Brushes.Gray;
                    button.BorderThickness = new Thickness(1);
                };

                ColorGrid.Children.Add(button);
            }
        }

        private void ApplyHexColor_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var color = HexColorTextBox.Text.Trim();
                if (!color.StartsWith("#"))
                {
                    color = "#" + color;
                }

                // Validate the color
                ColorConverter.ConvertFromString(color);
                SelectedColor = color;
                UpdatePreview();
            }
            catch
            {
                MessageBox.Show("Invalid hex color code. Please use format like #FF5733 or FF5733", 
                    "Invalid Color", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        private void UpdatePreview()
        {
            // Force UI update
            HexColorTextBox.Text = SelectedColor;
        }

        private void OK_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = true;
            Close();
        }

        private void Cancel_Click(object sender, RoutedEventArgs e)
        {
            DialogResult = false;
            Close();
        }
    }
}
