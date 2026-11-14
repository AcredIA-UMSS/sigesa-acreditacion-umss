#!/usr/bin/env python3
"""
Minimal SMTP catcher + HTTP inbox for SIGESA local/QA mail verification.

- Accepts any RCPT TO without AUTH (dev/qa only).
- Optional soft domain check via SMTP_ALLOWED_DOMAIN (default: umss.edu.bo).
- Stores raw RFC822 under DATA_DIR and serves a Gmail-like HTML inbox.
"""

from __future__ import annotations

import email
import email.policy
import json
import os
import re
import time
from email.message import Message
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import parse_qs, urlparse

from aiosmtpd.controller import Controller
from aiosmtpd.smtp import SMTP as SMTPServer
from aiosmtpd.smtp import Envelope, Session

DATA_DIR = Path(os.environ.get("SMTP_DATA_DIR", "/data"))
SMTP_HOST = os.environ.get("SMTP_HOST", "0.0.0.0")
SMTP_PORT = int(os.environ.get("SMTP_PORT", "2525"))
HTTP_PORT = int(os.environ.get("SMTP_HTTP_PORT", "8025"))
ALLOWED_DOMAIN = os.environ.get("SMTP_ALLOWED_DOMAIN", "umss.edu.bo").lower().strip()


def _ensure_data_dir() -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)


def _next_id() -> str:
    existing = sorted(DATA_DIR.glob("msg-*.eml"))
    if not existing:
        return "msg-000001"
    last = existing[-1].stem
    try:
        n = int(last.split("-")[1]) + 1
    except (IndexError, ValueError):
        n = int(time.time())
    return f"msg-{n:06d}"


def _parse_addresses(header_value: str | None) -> list[str]:
    if not header_value:
        return []
    return [a.strip() for a in header_value.split(",") if a.strip()]


def _message_summary(path: Path) -> dict[str, Any]:
    raw = path.read_bytes()
    msg = email.message_from_bytes(raw, policy=email.policy.default)
    return {
        "id": path.stem,
        "from": msg.get("From", ""),
        "to": _parse_addresses(msg.get("To")),
        "subject": msg.get("Subject", "(no subject)"),
        "date": msg.get("Date", ""),
        "size": path.stat().st_size,
    }


def _extract_bodies(msg: Message) -> tuple[str, str]:
    text_body = ""
    html_body = ""
    if msg.is_multipart():
        for part in msg.walk():
            ctype = part.get_content_type()
            disp = str(part.get("Content-Disposition", ""))
            if "attachment" in disp:
                continue
            try:
                payload = part.get_content()
            except Exception:
                payload = part.get_payload(decode=True)
                if isinstance(payload, bytes):
                    payload = payload.decode(part.get_content_charset() or "utf-8", errors="replace")
            if ctype == "text/plain" and not text_body:
                text_body = str(payload)
            elif ctype == "text/html" and not html_body:
                html_body = str(payload)
    else:
        try:
            payload = msg.get_content()
        except Exception:
            payload = msg.get_payload(decode=True)
            if isinstance(payload, bytes):
                payload = payload.decode(msg.get_content_charset() or "utf-8", errors="replace")
        if msg.get_content_type() == "text/html":
            html_body = str(payload)
        else:
            text_body = str(payload)
    return text_body, html_body


def _message_detail(path: Path) -> dict[str, Any]:
    raw = path.read_bytes()
    msg = email.message_from_bytes(raw, policy=email.policy.default)
    text_body, html_body = _extract_bodies(msg)
    return {
        **_message_summary(path),
        "text": text_body,
        "html": html_body,
        "raw": raw.decode("utf-8", errors="replace"),
    }


class CaptureHandler:
    async def handle_RCPT(
        self,
        server: SMTPServer,
        session: Session,
        envelope: Envelope,
        address: str,
        rcpt_options: list,
    ):
        if ALLOWED_DOMAIN and "@" in address:
            domain = address.rsplit("@", 1)[-1].lower()
            if domain != ALLOWED_DOMAIN:
                return f"550 Domain must be @{ALLOWED_DOMAIN}"
        envelope.rcpt_tos.append(address)
        return "250 OK"

    async def handle_DATA(self, server: SMTPServer, session: Session, envelope: Envelope):
        _ensure_data_dir()
        msg_id = _next_id()
        path = DATA_DIR / f"{msg_id}.eml"
        path.write_bytes(envelope.content)
        print(f"[SMTP] Accepted message from={session.peer} size={len(envelope.content)}", flush=True)
        return "250 Message accepted for delivery"


INDEX_HTML = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>SIGESA SMTP Inbox</title>
  <style>
    :root { --bg:#f3f4f5; --panel:#fff; --border:#e1e3e4; --primary:#003770; --muted:#737781; }
    * { box-sizing: border-box; }
    body { margin:0; font-family: Inter, system-ui, sans-serif; background:var(--bg); color:#191c1d; }
    header { background:var(--primary); color:#fff; padding:12px 20px; display:flex; justify-content:space-between; align-items:center; }
    header h1 { margin:0; font-size:18px; font-weight:600; }
    header button { background:#e30613; color:#fff; border:0; border-radius:8px; padding:8px 12px; cursor:pointer; margin-left:8px; }
    main { display:grid; grid-template-columns: 360px 1fr; height: calc(100vh - 52px); }
    .list { border-right:1px solid var(--border); background:var(--panel); overflow:auto; }
    .item { padding:12px 14px; border-bottom:1px solid var(--border); cursor:pointer; }
    .item:hover, .item.active { background:#e8f1ff; }
    .item .subj { font-weight:600; font-size:14px; }
    .item .meta { color:var(--muted); font-size:12px; margin-top:4px; }
    .detail { padding:20px; overflow:auto; }
    .empty { color:var(--muted); padding:40px; text-align:center; }
    .badge { background:#d6e3ff; color:var(--primary); border-radius:999px; padding:2px 8px; font-size:12px; }
    iframe { width:100%; min-height:60vh; border:1px solid var(--border); border-radius:8px; background:#fff; }
    pre { white-space:pre-wrap; background:#fff; border:1px solid var(--border); border-radius:8px; padding:12px; }
    .filters { display:flex; gap:8px; align-items:center; }
    .filters input { border:0; border-radius:6px; padding:6px 10px; min-width:180px; }
  </style>
</head>
<body>
  <header>
    <h1>SIGESA · Buzón SMTP de prueba</h1>
    <div class="filters">
      <input id="toFilter" type="search" placeholder="Filtrar por destinatario (cc@...)" />
      <span class="badge" id="count">0</span>
      <button type="button" onclick="clearAll()">Vaciar bandeja</button>
    </div>
  </header>
  <main>
    <section class="list" id="list"></section>
    <section class="detail" id="detail"><div class="empty">Seleccione un correo</div></section>
  </main>
  <script>
    let selected = null;
    async function loadList() {
      const to = document.getElementById('toFilter').value.trim();
      const url = to ? ('/api/messages?to=' + encodeURIComponent(to)) : '/api/messages';
      const res = await fetch(url);
      const data = await res.json();
      const list = document.getElementById('list');
      document.getElementById('count').textContent = data.messages.length + ' mails';
      if (!data.messages.length) {
        list.innerHTML = '<div class="empty">No hay correos capturados</div>';
        return;
      }
      list.innerHTML = data.messages.map(m => `
        <div class="item ${selected===m.id?'active':''}" onclick="openMsg('${m.id}')">
          <div class="subj">${escapeHtml(m.subject)}</div>
          <div class="meta">Para: ${escapeHtml((m.to||[]).join(', '))}</div>
          <div class="meta">${escapeHtml(m.date || '')}</div>
        </div>`).join('');
    }
    async function openMsg(id) {
      selected = id;
      const res = await fetch('/api/messages/' + id);
      const m = await res.json();
      const detail = document.getElementById('detail');
      detail.innerHTML = `
        <h2 style="margin-top:0">${escapeHtml(m.subject)}</h2>
        <p><strong>De:</strong> ${escapeHtml(m.from)}<br/>
           <strong>Para:</strong> ${escapeHtml((m.to||[]).join(', '))}<br/>
           <strong>Fecha:</strong> ${escapeHtml(m.date||'')}</p>
        ${m.html ? `<iframe sandbox="allow-same-origin" src="/api/messages/${id}/html"></iframe>` : `<pre>${escapeHtml(m.text||m.raw||'')}</pre>`}
      `;
      loadList();
    }
    async function clearAll() {
      if (!confirm('¿Vaciar toda la bandeja SMTP?')) return;
      await fetch('/api/messages', { method: 'DELETE' });
      selected = null;
      document.getElementById('detail').innerHTML = '<div class="empty">Seleccione un correo</div>';
      loadList();
    }
    function escapeHtml(s) {
      return String(s||'').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    }
    document.getElementById('toFilter').addEventListener('input', () => { loadList(); });
    loadList();
    setInterval(loadList, 4000);
  </script>
</body>
</html>
"""


class HttpHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt: str, *args: Any) -> None:
        print(f"[HTTP] {self.address_string()} {fmt % args}", flush=True)

    def _send(self, code: int, body: bytes, content_type: str) -> None:
        self.send_response(code)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def _json(self, code: int, payload: Any) -> None:
        raw = json.dumps(payload, ensure_ascii=False, indent=2).encode("utf-8")
        self._send(code, raw, "application/json; charset=utf-8")

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        path = parsed.path
        _ensure_data_dir()

        if path in ("/", "/index.html"):
            self._send(200, INDEX_HTML.encode("utf-8"), "text/html; charset=utf-8")
            return

        if path == "/api/messages":
            messages = [_message_summary(p) for p in sorted(DATA_DIR.glob("msg-*.eml"), reverse=True)]
            qs = parse_qs(parsed.query)
            to_filter = (qs.get("to") or [None])[0]
            if to_filter:
                to_filter = to_filter.lower()
                messages = [m for m in messages if any(to_filter in addr.lower() for addr in m["to"])]
            self._json(200, {"messages": messages})
            return

        m = re.match(r"^/api/messages/(msg-\d+)$", path)
        if m:
            file_path = DATA_DIR / f"{m.group(1)}.eml"
            if not file_path.exists():
                self._json(404, {"error": "not found"})
                return
            self._json(200, _message_detail(file_path))
            return

        m = re.match(r"^/api/messages/(msg-\d+)/html$", path)
        if m:
            file_path = DATA_DIR / f"{m.group(1)}.eml"
            if not file_path.exists():
                self._send(404, b"not found", "text/plain")
                return
            detail = _message_detail(file_path)
            html = detail["html"] or f"<pre>{detail['text'] or detail['raw']}</pre>"
            self._send(200, html.encode("utf-8"), "text/html; charset=utf-8")
            return

        m = re.match(r"^/api/messages/(msg-\d+)/raw$", path)
        if m:
            file_path = DATA_DIR / f"{m.group(1)}.eml"
            if not file_path.exists():
                self._send(404, b"not found", "text/plain")
                return
            self._send(200, file_path.read_bytes(), "message/rfc822")
            return

        self._json(404, {"error": "not found"})

    def do_DELETE(self) -> None:
        parsed = urlparse(self.path)
        if parsed.path != "/api/messages":
            self._json(404, {"error": "not found"})
            return
        _ensure_data_dir()
        for p in DATA_DIR.glob("msg-*.eml"):
            p.unlink(missing_ok=True)
        self._json(200, {"ok": True})


def main() -> None:
    _ensure_data_dir()
    handler = CaptureHandler()
    controller = Controller(handler, hostname=SMTP_HOST, port=SMTP_PORT)
    controller.start()
    print(f"[SMTP] listening on {SMTP_HOST}:{SMTP_PORT}", flush=True)

    httpd = ThreadingHTTPServer((SMTP_HOST, HTTP_PORT), HttpHandler)
    print(f"[HTTP] inbox UI on http://0.0.0.0:{HTTP_PORT}/", flush=True)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        httpd.shutdown()
        controller.stop()


if __name__ == "__main__":
    main()
