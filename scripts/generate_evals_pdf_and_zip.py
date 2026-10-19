#!/usr/bin/env python3
import os
import sys
import subprocess
import shutil
import re
import zipfile

def convert_md_to_html(md_path, html_path):
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Simple Markdown to HTML conversion for common elements
    html_lines = []
    in_table = False
    in_code = False
    
    # Process lines
    lines = content.split('\n')
    for line in lines:
        if line.startswith('```'):
            if in_code:
                html_lines.append('</pre>')
                in_code = False
            else:
                lang = line[3:].strip()
                html_lines.append(f'<pre class="code-block {lang}">')
                in_code = True
            continue
            
        if in_code:
            # Escape HTML in code blocks
            escaped = line.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            html_lines.append(escaped)
            continue
            
        if line.startswith('# '):
            html_lines.append(f'<h1>{line[2:]}</h1>')
        elif line.startswith('## '):
            html_lines.append(f'<h2>{line[3:]}</h2>')
        elif line.startswith('### '):
            html_lines.append(f'<h3>{line[4:]}</h3>')
        elif line.startswith('#### '):
            html_lines.append(f'<h4>{line[5:]}</h4>')
        elif line.startswith('---'):
            html_lines.append('<hr/>')
        elif line.startswith('|') and '|' in line[1:]:
            if '---' in line:
                continue # table header separator
            cells = [c.strip() for c in line.split('|')[1:-1]]
            if not in_table:
                html_lines.append('<table><thead><tr>')
                for c in cells:
                    html_lines.append(f'<th>{c}</th>')
                html_lines.append('</tr></thead><tbody>')
                in_table = True
            else:
                html_lines.append('<tr>')
                for c in cells:
                    html_lines.append(f'<td>{c}</td>')
                html_lines.append('</tr>')
        else:
            if in_table:
                html_lines.append('</tbody></table>')
                in_table = False
            if line.strip() == '':
                continue
            
            # Format bold, inline code, latex math
            l = line
            l = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', l)
            l = re.sub(r'`(.*?)`', r'<code>\1</code>', l)
            l = re.sub(r'\\\( (.*?) \\\)', r'<em>\1</em>', l)
            l = re.sub(r'\$\$(.*?)\$\$', r'<div class="math">\1</div>', l)
            l = re.sub(r'\$(.*?)\$', r'<em>\1</em>', l)
            
            if line.startswith('- '):
                html_lines.append(f'<ul><li>{l[2:]}</li></ul>')
            elif line.startswith('> '):
                html_lines.append(f'<blockquote>{l[2:]}</blockquote>')
            else:
                html_lines.append(f'<p>{l}</p>')
                
    if in_table:
        html_lines.append('</tbody></table>')

    body = '\n'.join(html_lines)

    css = """
    @page {
        size: A4;
        margin: 20mm;
    }
    body {
        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
        color: #1a1a1a;
        line-height: 1.5;
        font-size: 11pt;
        background-color: #ffffff;
    }
    h1 {
        font-size: 18pt;
        color: #0d47a1;
        border-bottom: 2px solid #0d47a1;
        padding-bottom: 6px;
        margin-top: 0;
    }
    h2 {
        font-size: 14pt;
        color: #1565c0;
        border-bottom: 1px solid #e0e0e0;
        padding-bottom: 4px;
        margin-top: 18px;
    }
    h3 {
        font-size: 12pt;
        color: #1e88e5;
        margin-top: 14px;
    }
    p, ul, ol {
        margin-bottom: 10px;
    }
    blockquote {
        background: #e3f2fd;
        border-left: 4px solid #1976d2;
        margin: 12px 0;
        padding: 8px 14px;
        font-style: italic;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        margin: 14px 0;
        font-size: 9.5pt;
    }
    th, td {
        border: 1px solid #d0d7de;
        padding: 6px 10px;
        text-align: left;
    }
    th {
        background-color: #f6f8fa;
        font-weight: bold;
        color: #24292f;
    }
    tr:nth-child(even) {
        background-color: #fcfcfc;
    }
    code {
        font-family: 'Courier New', Courier, monospace;
        background-color: #f3f4f6;
        padding: 2px 5px;
        border-radius: 3px;
        font-size: 9pt;
        color: #b91c1c;
    }
    pre.code-block {
        background-color: #1e1e1e;
        color: #d4d4d4;
        padding: 12px;
        border-radius: 6px;
        font-family: 'Courier New', Courier, monospace;
        font-size: 9pt;
        overflow-x: auto;
        white-space: pre-wrap;
    }
    .math {
        text-align: center;
        font-weight: bold;
        background: #f8f9fa;
        padding: 8px;
        margin: 10px 0;
        border-radius: 4px;
    }
    hr {
        border: 0;
        height: 1px;
        background: #e1e4e8;
        margin: 20px 0;
    }
    """

    full_html = f"""<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>INFORME DE EVALUACIÓN OFFLINE DE IA — ASISTENTE SIGESA</title>
    <style>{css}</style>
</head>
<body>
{body}
</body>
</html>
"""
    with open(html_path, 'w', encoding='utf-8') as f:
        f.write(full_html)
    print(f"Generated HTML: {html_path}")

def generate_pdf(html_path, pdf_path):
    cmd = [
        "google-chrome",
        "--headless",
        "--no-sandbox",
        "--disable-gpu",
        f"--print-to-pdf={pdf_path}",
        f"file://{os.path.abspath(html_path)}"
    ]
    subprocess.run(cmd, check=True)
    print(f"Generated PDF: {pdf_path}")

def generate_evidencia():
    evidencia_dir = "tools/evals/evidencia"
    os.makedirs(evidencia_dir, exist_ok=True)
    
    # Run gate check to get fresh evidence
    cmd = ["./run.sh", "gate", "--prompt-version", "v3"]
    res = subprocess.run(cmd, cwd="tools/evals", capture_output=True, text=True)
    
    log_content = f"--- TERMINAL EXECUTION LOG ---\nCommand: ./run.sh gate --prompt-version v3\nReturn Code: {res.returncode}\n\nSTDOUT:\n{res.stdout}\nSTDERR:\n{res.stderr}\n"
    log_file = os.path.join(evidencia_dir, "compuerta_v3_pass.log")
    with open(log_file, "w", encoding="utf-8") as f:
        f.write(log_content)
    print(f"Saved execution log: {log_file}")

def build_zip():
    zip_name = "M7_Evals_EquipoSIGESA.zip"
    if os.path.exists(zip_name):
        os.remove(zip_name)
        
    base_dir = "tools/evals"
    files_to_zip = []
    
    # 1. dataset/
    dataset_dir = os.path.join(base_dir, "datos/conocimiento")
    if os.path.exists(dataset_dir):
        for root, _, files in os.walk(dataset_dir):
            for file in files:
                if file.endswith(".jsonl"):
                    full_p = os.path.join(root, file)
                    arc_p = os.path.join("dataset", file)
                    files_to_zip.append((full_p, arc_p))
    if os.path.exists(os.path.join(base_dir, "DATASET_DORADO.md")):
        files_to_zip.append((os.path.join(base_dir, "DATASET_DORADO.md"), "dataset/DATASET_DORADO.md"))

    # 2. respuestas/
    resp_dir = os.path.join(base_dir, "artifacts/responses")
    if os.path.exists(resp_dir):
        for root, _, files in os.walk(resp_dir):
            for file in files:
                full_p = os.path.join(root, file)
                rel_p = os.path.relpath(full_p, resp_dir)
                arc_p = os.path.join("respuestas", rel_p)
                files_to_zip.append((full_p, arc_p))

    # 3. evals/
    for item in ["agente_evals.py", "config.py", "THRESHOLDS.md", "evals"]:
        full_p = os.path.join(base_dir, item)
        if os.path.isfile(full_p):
            files_to_zip.append((full_p, os.path.join("evals", item)))
        elif os.path.isdir(full_p):
            for root, _, files in os.walk(full_p):
                for file in files:
                    if not file.endswith(".pyc") and "__pycache__" not in root:
                        fp = os.path.join(root, file)
                        rel_p = os.path.relpath(fp, base_dir)
                        arc_p = os.path.join("evals", rel_p)
                        files_to_zip.append((fp, arc_p))

    # 4. evidencia/
    evid_dir = os.path.join(base_dir, "evidencia")
    if os.path.exists(evid_dir):
        for root, _, files in os.walk(evid_dir):
            for file in files:
                fp = os.path.join(root, file)
                rel_p = os.path.relpath(fp, evid_dir)
                arc_p = os.path.join("evidencia", rel_p)
                files_to_zip.append((fp, arc_p))

    # 5. M7_Evals_EquipoSIGESA.pdf
    pdf_p = "docs/qa/M7_Evals_EquipoSIGESA.pdf"
    if os.path.exists(pdf_p):
        files_to_zip.append((pdf_p, "M7_Evals_EquipoSIGESA.pdf"))

    # Write Zip
    with zipfile.ZipFile(zip_name, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for full_p, arc_p in files_to_zip:
            if os.path.exists(full_p):
                zipf.write(full_p, arc_p)
                
    print(f"Successfully generated ZIP: {zip_name} ({os.path.getsize(zip_name)} bytes)")

if __name__ == "__main__":
    md_file = "docs/qa/INFORME_ENTREGA_EVALS_OFFLINE.md"
    html_file = "/tmp/INFORME_ENTREGA_EVALS_OFFLINE.html"
    pdf_file = "docs/qa/M7_Evals_EquipoSIGESA.pdf"
    
    convert_md_to_html(md_file, html_file)
    generate_pdf(html_file, pdf_file)
    generate_evidencia()
    build_zip()
