#!/usr/bin/env python3
"""
Universal Icon Resizer
Interactive tool to resize any image to multiple sizes for browser extensions, apps, etc.
Supports common formats: PNG, JPG, JPEG, GIF, BMP, TIFF, WEBP
"""

from PIL import Image
import os
import tkinter as tk
from tkinter import filedialog, messagebox, simpledialog
import sys

class IconResizer:
    def __init__(self):
        # Default sizes for browser extensions
        self.default_sizes = [16, 32, 48, 64, 128, 256, 512]
        self.supported_formats = {
            '.png': 'PNG',
            '.jpg': 'JPEG', 
            '.jpeg': 'JPEG',
            '.gif': 'PNG',  # Convert GIF to PNG for better compatibility
            '.bmp': 'PNG',
            '.tiff': 'PNG',
            '.webp': 'PNG'
        }
        
    def select_input_image(self):
        """Let user select an input image file"""
        root = tk.Tk()
        root.withdraw()  # Hide the main window
        
        # Configure file dialog
        filetypes = [
            ('All Images', '*.png *.jpg *.jpeg *.gif *.bmp *.tiff *.webp'),
            ('PNG files', '*.png'),
            ('JPEG files', '*.jpg *.jpeg'), 
            ('GIF files', '*.gif'),
            ('BMP files', '*.bmp'),
            ('TIFF files', '*.tiff'),
            ('WEBP files', '*.webp'),
            ('All files', '*.*')
        ]
        
        file_path = filedialog.askopenfilename(
            title="Select Image to Resize",
            filetypes=filetypes
        )
        
        root.destroy()
        return file_path
    
    def select_output_directory(self):
        """Let user select output directory"""
        root = tk.Tk()
        root.withdraw()
        
        directory = filedialog.askdirectory(
            title="Select Output Directory for Resized Images"
        )
        
        root.destroy()
        return directory
    
    def get_custom_sizes(self):
        """Let user input custom sizes"""
        root = tk.Tk()
        root.withdraw()
        
        # Ask if user wants to use default sizes or custom sizes
        use_default = messagebox.askyesno(
            "Size Selection",
            f"Use default sizes: {', '.join(map(str, self.default_sizes))}?\n\n"
            "Click 'No' to enter custom sizes."
        )
        
        if use_default:
            sizes = self.default_sizes
        else:
            sizes_input = simpledialog.askstring(
                "Custom Sizes",
                "Enter sizes separated by commas (e.g., 16,32,48,128):",
                initialvalue=','.join(map(str, self.default_sizes))
            )
            
            if sizes_input:
                try:
                    sizes = [int(size.strip()) for size in sizes_input.split(',')]
                    sizes = [s for s in sizes if s > 0]  # Filter out invalid sizes
                except ValueError:
                    messagebox.showerror("Error", "Invalid size format. Using default sizes.")
                    sizes = self.default_sizes
            else:
                sizes = self.default_sizes
        
        root.destroy()
        return sizes
    
    def get_output_format(self):
        """Let user choose output format"""
        root = tk.Tk()
        root.withdraw()
        
        use_png = messagebox.askyesno(
            "Output Format",
            "Save as PNG format?\n\n"
            "PNG: Best for icons (transparency support)\n"
            "JPEG: Smaller files (no transparency)\n\n"
            "Click 'Yes' for PNG, 'No' for JPEG"
        )
        
        root.destroy()
        return 'PNG' if use_png else 'JPEG'
    
    def resize_image(self, input_path, output_dir, sizes, output_format):
        """Resize image to specified sizes"""
        try:
            # Get filename without extension
            base_name = os.path.splitext(os.path.basename(input_path))[0]
            
            # Open and process the image
            with Image.open(input_path) as img:
                print(f"📸 Original image: {os.path.basename(input_path)}")
                print(f"   Size: {img.size}")
                print(f"   Mode: {img.mode}")
                print(f"   Format: {img.format}")
                
                # Convert to RGBA for PNG or RGB for JPEG
                if output_format == 'PNG' and img.mode not in ['RGBA', 'LA']:
                    img = img.convert('RGBA')
                elif output_format == 'JPEG' and img.mode in ['RGBA', 'LA']:
                    # Create white background for JPEG
                    background = Image.new('RGB', img.size, (255, 255, 255))
                    if img.mode == 'RGBA':
                        background.paste(img, mask=img.split()[-1])  # Use alpha channel as mask
                    else:
                        background.paste(img)
                    img = background
                elif output_format == 'JPEG' and img.mode != 'RGB':
                    img = img.convert('RGB')
                
                successful_sizes = []
                failed_sizes = []
                
                # Create resized versions
                for size in sizes:
                    try:
                        # Use high-quality resampling
                        if hasattr(Image, 'Resampling'):
                            # PIL 10.0.0+
                            resized = img.resize((size, size), Image.Resampling.LANCZOS)
                        else:
                            # Older PIL versions
                            resized = img.resize((size, size), Image.LANCZOS)
                        
                        # Create filename
                        extension = '.png' if output_format == 'PNG' else '.jpg'
                        filename = f"{base_name}_{size}x{size}{extension}"
                        output_path = os.path.join(output_dir, filename)
                        
                        # Save with optimization
                        if output_format == 'PNG':
                            resized.save(output_path, "PNG", optimize=True)
                        else:
                            resized.save(output_path, "JPEG", optimize=True, quality=95)
                        
                        successful_sizes.append(size)
                        print(f"✅ Created {size}x{size}: {filename}")
                        
                    except Exception as e:
                        failed_sizes.append(size)
                        print(f"❌ Failed to create {size}x{size}: {str(e)}")
                
                return successful_sizes, failed_sizes
                
        except Exception as e:
            print(f"❌ Error opening image: {str(e)}")
            return [], sizes
    
    def run(self):
        """Main execution function"""
        print("🎨 Universal Icon Resizer")
        print("=" * 50)
        
        # Step 1: Select input image
        print("\n📁 Step 1: Select input image...")
        input_path = self.select_input_image()
        
        if not input_path:
            print("❌ No image selected. Exiting.")
            return
        
        if not os.path.exists(input_path):
            print(f"❌ File not found: {input_path}")
            return
        
        # Validate file format
        _, ext = os.path.splitext(input_path.lower())
        if ext not in self.supported_formats:
            print(f"❌ Unsupported format: {ext}")
            print(f"Supported formats: {', '.join(self.supported_formats.keys())}")
            return
        
        # Step 2: Select output directory
        print("\n📁 Step 2: Select output directory...")
        output_dir = self.select_output_directory()
        
        if not output_dir:
            print("❌ No output directory selected. Exiting.")
            return
        
        # Create output directory if it doesn't exist
        os.makedirs(output_dir, exist_ok=True)
        
        # Step 3: Get sizes
        print("\n📐 Step 3: Select sizes...")
        sizes = self.get_custom_sizes()
        
        if not sizes:
            print("❌ No sizes specified. Exiting.")
            return
        
        # Step 4: Get output format
        print("\n🎨 Step 4: Select output format...")
        output_format = self.get_output_format()
        
        # Step 5: Process image
        print(f"\n🔄 Processing image...")
        print(f"   Input: {input_path}")
        print(f"   Output: {output_dir}")
        print(f"   Sizes: {sizes}")
        print(f"   Format: {output_format}")
        print("-" * 50)
        
        successful_sizes, failed_sizes = self.resize_image(
            input_path, output_dir, sizes, output_format
        )
        
        # Show results
        print("\n" + "=" * 50)
        print("📊 RESULTS:")
        
        if successful_sizes:
            print(f"✅ Successfully created {len(successful_sizes)} sizes:")
            for size in successful_sizes:
                print(f"   - {size}x{size} pixels")
        
        if failed_sizes:
            print(f"❌ Failed to create {len(failed_sizes)} sizes:")
            for size in failed_sizes:
                print(f"   - {size}x{size} pixels")
        
        print(f"\n📁 Output location: {output_dir}")
        
        # Show final message
        if successful_sizes:
            root = tk.Tk()
            root.withdraw()
            messagebox.showinfo(
                "Success!",
                f"Successfully created {len(successful_sizes)} icon sizes!\n\n"
                f"Location: {output_dir}"
            )
            root.destroy()

def main():
    """Main entry point"""
    try:
        resizer = IconResizer()
        resizer.run()
    except KeyboardInterrupt:
        print("\n\n❌ Operation cancelled by user.")
    except Exception as e:
        print(f"\n❌ Unexpected error: {str(e)}")
        input("Press Enter to exit...")

if __name__ == "__main__":
    main()