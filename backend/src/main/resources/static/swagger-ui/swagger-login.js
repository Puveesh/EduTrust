// ============================================================================
// Sentinel-Ledger (Project EduTrust) - Swagger UI Quick Login & Auto-Authorize
// ============================================================================
(function() {
  'use strict';

  // ==========================================================================
  // MASTER TOGGLE: Set to false to remove the Quick Login button completely.
  // Set to true to re-enable it.
  // ==========================================================================
  const ENABLE_QUICK_LOGIN = false;
  if (!ENABLE_QUICK_LOGIN) return;

  function applyTokenToSwagger(token) {

    if (!window.ui) return;

    try {
      if (window.ui.authActions && window.ui.authActions.authorize) {
        window.ui.authActions.authorize({
          BearerAuth: {
            name: 'BearerAuth',
            schema: {
              type: 'http',
              scheme: 'bearer',
              bearerFormat: 'JWT'
            },
            value: token
          }
        });
      }
    } catch (e) {
      console.warn('authActions.authorize failed:', e);
    }

    try {
      if (window.ui.preauthorizeApiKey) {
        window.ui.preauthorizeApiKey('BearerAuth', token);
      }
    } catch (e) {
      console.warn('preauthorizeApiKey failed:', e);
    }
  }

  function clearSwaggerAuth() {
    localStorage.removeItem('swagger_edutrust_token');
    localStorage.removeItem('swagger_edutrust_user');
    if (window.ui && window.ui.authActions && window.ui.authActions.logout) {
      try {
        window.ui.authActions.logout(['BearerAuth']);
      } catch (e) {}
    }
    updateStatusUI(null, null);
    showToast('Swagger UI authorization cleared.', 'info');
  }

  function updateStatusUI(username, role) {
    const statusContainer = document.getElementById('sw-auth-status-container');
    if (!statusContainer) return;

    if (!username) {
      statusContainer.innerHTML = '';
      return;
    }

    statusContainer.innerHTML = `
      <div style="margin-left: 8px; font-size: 11px; background: #ecfdf5; border: 1px solid #6ee7b7; color: #065f46; padding: 4px 10px; border-radius: 4px; display: inline-flex; align-items: center; gap: 6px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); font-family: sans-serif;">
        <span style="color: #059669; font-weight: bold;">●</span>
        <span>Authorized: <strong>${escapeHtml(username)}</strong> <span style="opacity: 0.8; font-size: 10px;">(${escapeHtml(role)})</span></span>
        <button id="sw-logout-btn" type="button" style="background: none; border: none; color: #dc2626; cursor: pointer; text-decoration: underline; font-size: 10px; margin-left: 4px; padding: 0;">Logout</button>
      </div>
    `;

    const logoutBtn = document.getElementById('sw-logout-btn');
    if (logoutBtn) {
      logoutBtn.onclick = (e) => {
        e.preventDefault();
        clearSwaggerAuth();
      };
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  function showToast(message, type = 'success') {
    const existing = document.getElementById('sw-custom-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.id = 'sw-custom-toast';
    const bg = type === 'error' ? '#ef4444' : (type === 'info' ? '#3b82f6' : '#10b981');
    toast.style.cssText = `
      position: fixed;
      bottom: 24px;
      right: 24px;
      background: ${bg};
      color: white;
      padding: 12px 20px;
      border-radius: 8px;
      font-size: 13px;
      font-weight: 600;
      box-shadow: 0 10px 15px -3px rgba(0,0,0,0.3);
      z-index: 9999999;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      animation: fadeIn 0.2s ease-out;
    `;
    toast.innerText = message;
    document.body.appendChild(toast);
    setTimeout(() => {
      if (toast && toast.parentNode) toast.parentNode.removeChild(toast);
    }, 4000);
  }

  function openModal() {
    let modal = document.getElementById('swagger-login-modal');
    if (modal) {
      modal.style.display = 'flex';
      return;
    }

    modal = document.createElement('div');
    modal.id = 'swagger-login-modal';
    modal.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(15, 23, 42, 0.65);
      backdrop-filter: blur(4px);
      z-index: 999999;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    `;

    modal.innerHTML = `
      <div style="background: #ffffff; border-radius: 12px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25); width: 440px; max-width: 90vw; padding: 24px; position: relative; border: 1px solid #e2e8f0; color: #1e293b;">
        <!-- Header -->
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <div style="width: 36px; height: 36px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px;">
              🛡️
            </div>
            <div>
              <h3 style="margin: 0; font-size: 17px; font-weight: 700; color: #0f172a;">EduTrust API Login</h3>
              <p style="margin: 2px 0 0; font-size: 11px; color: #64748b;">Authenticate & Auto-Authorize Swagger UI with JWT</p>
            </div>
          </div>
          <button id="sw-login-close" type="button" style="background: none; border: none; font-size: 22px; cursor: pointer; color: #94a3b8; line-height: 1; padding: 2px 6px;">&times;</button>
        </div>

        <!-- Quick Presets -->
        <div style="margin-bottom: 16px;">
          <div style="font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b; margin-bottom: 8px;">
            ⚡ Quick 1-Click Login:
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
            <button type="button" class="sw-preset-btn" data-user="admin" data-pass="Admin@123" style="text-align: left; padding: 8px 10px; border-radius: 6px; border: 1px solid #cbd5e1; background: #f8fafc; cursor: pointer; transition: all 0.15s;">
              <div style="font-weight: 700; font-size: 12px; color: #1e293b;">AICTE Admin</div>
              <div style="font-size: 10px; color: #64748b;">admin / Admin@123</div>
            </button>

            <button type="button" class="sw-preset-btn" data-user="registrar_delhi" data-pass="Registrar@123" style="text-align: left; padding: 8px 10px; border-radius: 6px; border: 1px solid #cbd5e1; background: #f8fafc; cursor: pointer; transition: all 0.15s;">
              <div style="font-weight: 700; font-size: 12px; color: #1e293b;">Delhi Registrar</div>
              <div style="font-size: 10px; color: #64748b;">registrar_delhi</div>
            </button>

            <button type="button" class="sw-preset-btn" data-user="registrar_maha" data-pass="Registrar@123" style="text-align: left; padding: 8px 10px; border-radius: 6px; border: 1px solid #cbd5e1; background: #f8fafc; cursor: pointer; transition: all 0.15s;">
              <div style="font-weight: 700; font-size: 12px; color: #1e293b;">Maha Registrar</div>
              <div style="font-size: 10px; color: #64748b;">registrar_maha</div>
            </button>

            <button type="button" class="sw-preset-btn" data-user="verifier_corp" data-pass="Verifier@123" style="text-align: left; padding: 8px 10px; border-radius: 6px; border: 1px solid #cbd5e1; background: #f8fafc; cursor: pointer; transition: all 0.15s;">
              <div style="font-weight: 700; font-size: 12px; color: #1e293b;">Verifier</div>
              <div style="font-size: 10px; color: #64748b;">verifier_corp</div>
            </button>
          </div>
        </div>

        <div style="display: flex; align-items: center; margin: 16px 0; text-align: center; color: #94a3b8; font-size: 11px;">
          <div style="flex: 1; height: 1px; background: #e2e8f0;"></div>
          <span style="padding: 0 8px; font-weight: 600;">OR ENTER CREDENTIALS</span>
          <div style="flex: 1; height: 1px; background: #e2e8f0;"></div>
        </div>

        <!-- Form -->
        <form id="sw-login-form">
          <div style="margin-bottom: 12px;">
            <label style="display: block; font-size: 12px; font-weight: 600; color: #334155; margin-bottom: 4px;">Username</label>
            <input type="text" id="sw-user-input" value="admin" style="width: 100%; box-sizing: border-box; padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; outline: none;" required />
          </div>
          <div style="margin-bottom: 16px;">
            <label style="display: block; font-size: 12px; font-weight: 600; color: #334155; margin-bottom: 4px;">Password</label>
            <div style="position: relative;">
              <input type="password" id="sw-pass-input" value="Admin@123" style="width: 100%; box-sizing: border-box; padding: 8px 36px 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; outline: none;" required />
              <button type="button" id="sw-toggle-pass" style="position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: #64748b; font-size: 14px; padding: 4px;">👁️</button>
            </div>
          </div>
          <div id="sw-error-msg" style="display: none; padding: 8px 12px; border-radius: 6px; background: #fef2f2; border: 1px solid #fecaca; color: #ef4444; font-size: 12px; margin-bottom: 14px;"></div>
          <button type="submit" id="sw-submit-btn" style="width: 100%; background: #2563eb; color: #ffffff; border: none; padding: 10px; border-radius: 6px; font-weight: 600; font-size: 14px; cursor: pointer; transition: background-color 0.15s;">
            🔐 Sign In & Authorize Swagger
          </button>
        </form>
      </div>
    `;

    document.body.appendChild(modal);

    // Event listeners
    document.getElementById('sw-login-close').onclick = closeModal;
    modal.onclick = (e) => {
      if (e.target === modal) closeModal();
    };

    // Toggle password
    const toggleBtn = document.getElementById('sw-toggle-pass');
    const passInput = document.getElementById('sw-pass-input');
    toggleBtn.onclick = () => {
      if (passInput.type === 'password') {
        passInput.type = 'text';
        toggleBtn.innerText = '🙈';
      } else {
        passInput.type = 'password';
        toggleBtn.innerText = '👁️';
      }
    };

    // Preset buttons
    const presets = modal.querySelectorAll('.sw-preset-btn');
    presets.forEach(btn => {
      btn.onmouseenter = () => { btn.style.background = '#e2e8f0'; };
      btn.onmouseleave = () => { btn.style.background = '#f8fafc'; };
      btn.onclick = () => {
        const u = btn.getAttribute('data-user');
        const p = btn.getAttribute('data-pass');
        document.getElementById('sw-user-input').value = u;
        document.getElementById('sw-pass-input').value = p;
        doLogin(u, p);
      };
    });

    // Form submit
    document.getElementById('sw-login-form').onsubmit = (e) => {
      e.preventDefault();
      const u = document.getElementById('sw-user-input').value.trim();
      const p = document.getElementById('sw-pass-input').value;
      doLogin(u, p);
    };
  }

  function closeModal() {
    const modal = document.getElementById('swagger-login-modal');
    if (modal) modal.style.display = 'none';
  }

  async function doLogin(username, password) {
    const submitBtn = document.getElementById('sw-submit-btn');
    const errDiv = document.getElementById('sw-error-msg');
    if (errDiv) errDiv.style.display = 'none';
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.innerText = 'Authenticating...';
    }

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || 'Login failed: HTTP ' + res.status);
      }
      const token = data.token;
      if (!token) {
        throw new Error('No token returned in authentication response');
      }

      // Apply authorization to Swagger UI
      applyTokenToSwagger(token);

      // Persist to localStorage
      localStorage.setItem('swagger_edutrust_token', token);
      localStorage.setItem('swagger_edutrust_user', JSON.stringify({
        username: data.username,
        fullName: data.fullName,
        role: data.role
      }));

      updateStatusUI(data.username, data.role);
      closeModal();
      showToast(`✓ Logged in as ${data.fullName} (${data.role}). Swagger UI authorized!`, 'success');
    } catch (err) {
      if (errDiv) {
        errDiv.innerText = err.message || 'Authentication error';
        errDiv.style.display = 'block';
      } else {
        alert('Authentication failed: ' + err.message);
      }
    } finally {
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerText = '🔐 Sign In & Authorize Swagger';
      }
    }
  }

  function attachLoginButton(authBtn) {
    if (document.getElementById('sw-open-login-btn')) return;

    const container = document.createElement('div');
    container.id = 'sw-login-container';
    container.style.cssText = 'display: inline-flex; align-items: center; margin-left: 10px; vertical-align: middle;';

    const loginBtn = document.createElement('button');
    loginBtn.id = 'sw-open-login-btn';
    loginBtn.className = 'btn';
    loginBtn.type = 'button';
    loginBtn.innerHTML = '<span>🔑 Quick Login</span>';
    loginBtn.style.cssText = `
      background-color: #10b981;
      color: white;
      border: 2px solid #059669;
      font-weight: 700;
      border-radius: 4px;
      padding: 4px 14px;
      display: inline-flex;
      align-items: center;
      cursor: pointer;
      height: 35px;
      box-sizing: border-box;
      font-size: 12px;
      text-transform: uppercase;
      margin: 0;
      box-shadow: 0 1px 2px rgba(0,0,0,0.08);
      transition: all 0.15s ease;
      font-family: sans-serif;
    `;
    loginBtn.onmouseenter = () => { loginBtn.style.backgroundColor = '#059669'; };
    loginBtn.onmouseleave = () => { loginBtn.style.backgroundColor = '#10b981'; };
    loginBtn.onclick = openModal;

    const statusBadge = document.createElement('div');
    statusBadge.id = 'sw-auth-status-container';
    statusBadge.style.cssText = 'display: inline-flex; align-items: center;';

    container.appendChild(loginBtn);
    container.appendChild(statusBadge);

    authBtn.insertAdjacentElement('afterend', container);

    // Auto-restore saved session if token exists
    const token = localStorage.getItem('swagger_edutrust_token');
    const userJson = localStorage.getItem('swagger_edutrust_user');
    if (token) {
      applyTokenToSwagger(token);
      if (userJson) {
        try {
          const u = JSON.parse(userJson);
          updateStatusUI(u.username, u.role);
        } catch (e) {}
      }
    }
  }

  function initQuickLogin() {
    // Check periodically until Swagger UI renders the Authorize button
    const timer = setInterval(() => {
      const authBtn = document.querySelector('.auth-wrapper .authorize, .scheme-container .authorize, button.authorize');
      if (authBtn && !document.getElementById('sw-open-login-btn')) {
        clearInterval(timer);
        attachLoginButton(authBtn);
      }
    }, 250);

    // Mutation observer for re-renders
    const observer = new MutationObserver(() => {
      const authBtn = document.querySelector('.auth-wrapper .authorize, .scheme-container .authorize, button.authorize');
      if (authBtn && !document.getElementById('sw-open-login-btn')) {
        attachLoginButton(authBtn);
      }
    });

    if (document.body) {
      observer.observe(document.body, { childList: true, subtree: true });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initQuickLogin);
  } else {
    initQuickLogin();
  }
})();
