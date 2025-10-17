using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using WindowsVault.Models;

namespace WindowsVault.Views
{
    public partial class TagSelectionDialog : Window
    {
        public class SelectableTag
        {
            public Tag Tag { get; set; }
            public string Name => Tag.Name;
            public string Color => Tag.Color;
            public bool IsSelected { get; set; }
        }

        public ObservableCollection<SelectableTag> AvailableTags { get; set; } = new();
        public bool TagsAssigned { get; private set; } = false;

        public TagSelectionDialog(IEnumerable<Tag> tags)
        {
            InitializeComponent();
            DataContext = this;

            foreach (var tag in tags)
            {
                AvailableTags.Add(new SelectableTag { Tag = tag, IsSelected = false });
            }
        }

        public List<Tag> GetSelectedTags()
        {
            return AvailableTags.Where(t => t.IsSelected).Select(t => t.Tag).ToList();
        }

        private void AssignButton_Click(object sender, RoutedEventArgs e)
        {
            TagsAssigned = true;
            DialogResult = true;
            Close();
        }

        private void SkipButton_Click(object sender, RoutedEventArgs e)
        {
            TagsAssigned = false;
            DialogResult = false;
            Close();
        }
    }
}
