# Snippy UI Design Guide

## 🎨 Modern UI Update - Design System

### Color Palette

#### Primary Colors
```
Blue (Primary):     #0078d4 → #0086e8 (gradient)
Blue Hover:         #1084d8 → #1a96f0 (gradient)
Blue Pressed:       #006cbd → #005a9e (gradient)
```

#### Background Colors
```
Main Background:    #1e1e1e → #2b2b2b (gradient)
Card Background:    #353535 (solid)
Info Card:          #2a2a2a (solid)
Secondary BG:       #3c3c3c (inputs)
```

#### Text Colors
```
Primary Text:       #ffffff (white)
Secondary Text:     #dddddd (light gray)
Tertiary Text:      #999999 (medium gray)
Muted Text:         #888888 (dark gray)
```

#### Accent Colors
```
Success Green:      #00ff00 (recording mode)
Error Red:          #d41400 → #e84545 (gradient)
Warning:            #ff9800
Info:               #00a0e3
```

#### Border Colors
```
Default:            #555555
Hover:              #0078d4
Focus:              #0086e8
Card Border:        #454545
```

### Typography

#### Font Sizes
```
Title:              24pt (Bold)
Section Heading:    15pt (Bold)
Body:               14pt (Regular/Medium)
Label:              13pt (Medium)
Small:              11pt (Regular)
Timer:              22pt (Bold)
```

#### Font Weights
```
Bold:               700
Medium:             500
Regular:            400
```

### Spacing System

#### Margins & Padding
```
Window Padding:     40px 35px
Card Padding:       25px 20px
Info Card Padding:  20px 15px
Element Spacing:    15-25px
Button Padding:     12px 24px
```

#### Border Radius
```
Large (Cards):      12px
Medium (Buttons):   8px
Small (Inputs):     6px
Overlay:            15px
```

### Component Specifications

#### Buttons
```
Primary Button:
- Height: 55px (action buttons), 45px (secondary)
- Gradient: #0086e8 → #0078d4
- Border Radius: 8px
- Font Size: 14pt Bold
- Cursor: Pointer
- Hover: Lighter gradient
- Pressed: Darker gradient

Secondary Button:
- Gradient: #505050 → #3c3c3c
- Border: 1px solid #606060
- Same other properties
```

#### Dropdown (QComboBox)
```
Height: 45px
Background: #3c3c3c
Border: 2px solid #555555
Border (Hover): 2px solid #0078d4
Border (Focus): 2px solid #0086e8
Border Radius: 6px
Padding: 10px, 30px right
Font: 14pt Medium

Dropdown List:
- Background: #2d2d2d
- Border: 2px solid #0078d4
- Item Height: 35px minimum
- Item Padding: 8px 12px
- Hover Background: #0078d4
- Selected Background: #0086e8
- Text Color: #ffffff (always visible!)
```

#### Input Fields (QSpinBox)
```
Height: 45px
Background: #3c3c3c
Border: 2px solid #555555
Border (Hover): 2px solid #0078d4
Padding: 10px
Font: 14pt

Up/Down Buttons:
- Background: #505050
- Hover: #0078d4
- Size: 20px
- Border Radius: 3px
```

#### Cards
```
Settings Card:
- Background: #353535
- Border: 1px solid #454545
- Border Radius: 12px
- Padding: 25px 20px

Info Card:
- Background: #2a2a2a
- Border: 1px solid #404040
- Border Radius: 10px
- Padding: 20px 15px
```

#### Header
```
Background: Gradient (#0078d4 → #00a0e3)
Border Radius: 12px
Padding: 25px 20px
Title: 24pt Bold White
Subtitle: 13pt Medium #e0f0ff
```

#### Recording Overlay
```
Background: Gradient (#2b2b2b → #1e1e1e)
Border: 2px solid #ff4444
Border Radius: 15px
Position: Top-right (20px from edge)
Size: 250px × 140px
Shadow: Subtle drop shadow

Stop Button:
- Gradient: #e84545 → #d41400
- Hover: #ff5555 → #e41400
- Height: 44px
- Border Radius: 8px
```

### Selection Overlay Styles

#### Screenshot Mode
```
Background Overlay: rgba(0, 0, 0, 100)
Selection Border: #0078d4, 3px
Corner Handles: 8px diameter, #0078d4
Dimension Badge: #0078d4 background, white text
Instructions: Black background (180 alpha)
```

#### Recording Mode
```
Background Overlay: rgba(0, 0, 0, 60) - lighter!
Selection Border: #00ff00, 3px - green!
Corner Handles: 8px diameter, #00ff00
Dimension Badge: Green background, white text
Instructions: Green background (200 alpha)
```

### Interactive States

#### Hover Effects
```
Buttons: Lighter gradient
Dropdowns: Border changes to blue, slight bg lighten
Inputs: Border changes to blue
Cards: Subtle border color change (optional)
```

#### Focus States
```
Inputs/Dropdowns: Border #0086e8 (brighter blue)
Outline: None (custom border instead)
```

#### Pressed States
```
Buttons: Darker gradient
Inputs: No change
```

### Accessibility

#### Contrast Ratios
```
White on #0078d4: 4.5:1 ✓
White on #353535: 12.6:1 ✓
#dddddd on #353535: 10.8:1 ✓
Dropdown items: Always white on dark ✓
```

#### Cursor Changes
```
Buttons: PointingHandCursor
Inputs: IBeamCursor
Selection: CrossCursor
Default: ArrowCursor
```

### Animations (Future Enhancement)
```
Button Hover: 150ms ease
Border Color: 200ms ease
Background: 200ms ease
Fade In: 300ms ease
```

### Window Properties
```
Size: 550 × 650
Minimum: 500 × 600
Position: Center screen
Resizable: Yes
Background: Gradient
```

### Visual Hierarchy

```
Level 1: Title (24pt Bold, Gradient background)
Level 2: Section Headings (15pt Bold)
Level 3: Labels (13pt Medium)
Level 4: Values/Info (11pt Regular)
```

### Consistency Rules

1. **All interactive elements have hover states**
2. **All buttons have pointer cursor**
3. **All inputs have focus states with blue border**
4. **All cards have rounded corners (12px)**
5. **All gradients go top to bottom or left to right**
6. **All spacing is multiples of 5px**
7. **All text is easily readable (high contrast)**
8. **All dropdown items are white text on dark background**

### Dark Theme Implementation

This is a **true dark theme** with:
- Dark backgrounds (#1e-#35 range)
- Light text (#dd-#ff range)
- Blue accents for interaction
- High contrast for readability
- No pure black (easier on eyes)
- No pure white (softer appearance)

### Future Enhancements

- [ ] Add subtle shadows to cards
- [ ] Implement smooth transitions
- [ ] Add pulse animation to recording indicator
- [ ] Add tooltips with custom styling
- [ ] Add keyboard shortcuts display
- [ ] Add settings persistence
- [ ] Add theme customization options
- [ ] Add window transparency option

---

**Design Philosophy:**
Modern, clean, professional, easy to use, high contrast, accessible, consistent, and beautiful.
