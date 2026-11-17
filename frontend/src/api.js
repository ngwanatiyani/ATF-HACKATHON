export function apiRoot() {
  return (localStorage.pulseApi || "").trim().replace(/\/$/, "");
}

function headers(json) {
  const h = {};
  if (json) h["Content-Type"] = "application/json";
  if (sessionStorage.pulsePhone) {
    h.Authorization = "Basic " + btoa(unescape(encodeURIComponent(sessionStorage.pulsePhone + ":" + sessionStorage.pulsePass)));
  }
  return h;
}

export async function call(path, body) {
  let res;
  try {
    res = await fetch(apiRoot() + path, {
      method: body ? "POST" : "GET",
      headers: headers(!!body),
      body: body ? JSON.stringify(body) : undefined
    });
  } catch {
    throw new Error("Cannot reach the API at " + (apiRoot() || "http://localhost:8081"));
  }
  if (res.status === 401) throw new Error("Wrong phone or password");
  const text = await res.text();
  let data = text;
  try { data = text ? JSON.parse(text) : null; } catch { /* plain text */ }
  if (!res.ok) throw new Error((data && (data.detail || data.message)) || res.status);
  return data;
}
