# ExpenseTrackerApp

A clean and lightweight Java-based Expense Tracker designed to help users record and manage expenses easily. Built with simplicity in mind — perfect for beginners or as a foundation for more advanced tools.

## 📛 Badges
![Java](https://img.shields.io/badge/Java-24-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/Project-Active-brightgreen)

---

## 🚀 Features
- Add, view, and manage expenses
- Beginner-friendly Java codebase
- Optional web interface (index.html)
- MIT License — free to use and modify

---

## 📂 Project Structure
ExpenseTrackerApp/
 ├── Main.java          # Core Java logic
 ├── index.html         # Web entry page for GitHub Pages
 ├── README.md          # Documentation
 └── LICENSE            # MIT License

---

## 🛠 Requirements
- JDK 17+ (Recommended: JDK 24)
- Terminal or any Java IDE
- Optional browser (for GitHub Pages)

---

## ▶️ How to Run

### Run using terminal
javac Main.java
java Main

### Run using an IDE
1. Open folder in IntelliJ / VS Code / Eclipse
2. Build & run Main.java

---

## 🌐 Hosting on GitHub Pages
To publish the project as a website:

1. Ensure index.html is in the root folder  
2. Go to Settings → Pages  
3. Select:
   - Branch: main
   - Folder: /(root)
4. Save and wait for deployment  

---

## 🔮 Future Enhancements
- Persistent storage (JSON / SQLite)
- Dashboard UI with charts
- Categorization & filters
- Export as CSV / PDF
- Mobile-friendly interface

---

## 📄 License
Licensed under the MIT License.

---

# index.html (include this in your repo)
```html
<!DOCTYP
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>ExpenseTrackerApp</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background: #111;
      color: #fff;
      margin: 0;
      padding: 40px;
      text-align: center;
    }
    h1 { color: #4CAF50; }
    .card {
      background: #222;
      padding: 20px;
      margin: auto;
      width: 60%;
      border-radius: 10px;
      box-shadow: 0 0 10px #000;
    }
    a {
      display: inline-block;
      margin-top: 20px;
      color: #4CAF50;
      font-weight: bold;
      text-decoration: none;
    }
  </style>
</head>
<body>
  <h1>ExpenseTrackerApp</h1>
  <div class="card">
    <p>A simple Java-based expense tracker. Run Main.java to begin using the application.</p>
    <a href="https://github.com/shloook/ExpenseTrackerApp" target="_blank">View GitHub Repository →</a>
  </div>
</body>
</html>

