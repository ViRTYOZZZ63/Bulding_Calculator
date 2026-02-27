const API_BASE = 'http://localhost:8080/api';
const HERO_IMAGES = ['./media/avel-chuklanov-IB0VA6VdqBw-unsplash.jpg', './media/brett-jordan-PFr50OBMowU-unsplash.jpg', './media/glenov-brankovic-DWp5nUqTn6E-unsplash.jpg'];
const GALLERY_IMAGES = [
  { src: './media/scott-blake-wq7oyx_Kx-4-unsplash.jpg', title: 'Архитектурный концепт' },
  { src: './media/jesse-orrico-L94dWXNKwrY-unsplash.jpg', title: 'Планирование материалов' },
  { src: './media/pop-zebra-wp81DxKUd1E-unsplash.jpg', title: 'Контроль закупок' }
];

const storage = {
  get(key, fallback = null) {
    try {
      const value = localStorage.getItem(key);
      return value === null ? fallback : value;
    } catch {
      return fallback;
    }
  },
  set(key, value) {
    try { localStorage.setItem(key, value); } catch {}
  },
  remove(key) {
    try { localStorage.removeItem(key); } catch {}
  }
};

const state = {
  tab: 'home',
  heroIndex: 0,
  heroTimer: null,
  email: storage.get('login', 'manager') || 'manager',
  password: 'password',
  authInfo: storage.get('login', 'guest') || 'guest',
  authenticated: Boolean(storage.get('accessToken', '')),
  loadingAuth: false,
  materials: [],
  loadingMaterials: false,
  clients: [],
  selectedClientId: '',
  address: 'г. Москва, ул. Строителей, 9',
  calcId: '',
  cabinetCalcs: [],
  materialFilter: '',
  floorCount: 1,
  frameFloor: { floorHeight: 2.8, perimeter: 48, foundationArea: 120, extWallThickness: 'MM_150', innerWallLength: 36, intWallThickness: 'MM_100', extOsb: 'OSB 12 мм', extVapor: 'Ондутис', extWindBarrier: 'Ветро-влагозащитная мембрана Brane А', extInsulation: 'Эковер 150мм', intOsb: 'OSB 9 мм', ceilingThickness: 'MM_200', ceilingOsb: 'OSB 9 мм', ceilingVapor: 'Пароизоляция Axton (b)', ceilingWindBarrier: 'Гидро-ветрозащита Тип А', ceilingInsulation: 'Минеральная вата 200мм', windows: [{ width: 1.4, height: 1.4, count: 8 }], externalDoors: [{ width: 1, height: 2.1, count: 2 }], internalDoors: [{ width: 0.9, height: 2.1, count: 6 }] },
  foundation: { externalPerimeter: 48, innerWallLength: 36, pileType: 'Бетонные сваи 200*200*3000', concreteGrade: 'М300 (В22.5)' },
  createClient: { lastName: '', firstName: '', patronymic: '', phone: '', email: '', address: '' },
  cart: JSON.parse(storage.get('cart', '[]')),
  resultMessage: 'Готово к работе. Войдите, чтобы открыть личный кабинет и расчёты.'
};

const escapeHtml = (str = '') => String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;');
const authHeaders = () => {
  const token = storage.get('accessToken', '');
  return token ? { Authorization: `Bearer ${token}` } : {};
};
const filteredMaterials = () => {
  const query = state.materialFilter.trim().toLowerCase();
  return query ? state.materials.filter((m) => `${m.name} ${m.unit}`.toLowerCase().includes(query)) : state.materials;
};
const totalCart = () => state.cart.reduce((sum, item) => sum + Number(item.currentPrice || 0), 0);

async function refreshSession() {
  const refreshToken = storage.get('refreshToken', '');
  if (!refreshToken) return false;
  try {
    const response = await fetch(`${API_BASE}/auth/refresh`, { method: 'POST', headers: { 'X-Refresh-Token': refreshToken } });
    if (!response.ok) return false;
    const data = await response.json();
    storage.set('accessToken', data.accessToken);
    storage.set('refreshToken', data.refreshToken);
    storage.set('login', data.login);
    state.authInfo = data.login;
    state.authenticated = true;
    return true;
  } catch {
    return false;
  }
}

async function apiFetch(path, options = {}, retry = true) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { ...(options.headers || {}), ...authHeaders() }
  });

  if (response.status === 401 && retry) {
    const refreshed = await refreshSession();
    if (refreshed) return apiFetch(path, options, false);
    logout(false);
  }

  return response;
}

function setMessage(message) {
  state.resultMessage = message;
  const target = document.querySelector('.result-msg');
  if (target) target.textContent = message;
}

function logout(withRender = true) {
  storage.remove('accessToken');
  storage.remove('refreshToken');
  storage.remove('login');
  state.authInfo = 'guest';
  state.authenticated = false;
  state.clients = [];
  state.cabinetCalcs = [];
  if (withRender) {
    setMessage('Сессия завершена. Войдите снова.');
    render();
  }
}

async function login(e) {
  e.preventDefault();
  state.loadingAuth = true;
  render();
  try {
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login: state.email, password: state.password })
    });
    if (!response.ok) throw new Error();
    const data = await response.json();
    storage.set('accessToken', data.accessToken);
    storage.set('refreshToken', data.refreshToken);
    storage.set('login', data.login);
    state.authInfo = data.login;
    state.authenticated = true;
    setMessage(`Вы вошли как ${data.login}. Личный кабинет открыт.`);
  } catch {
    setMessage('Не удалось войти. Проверьте login/password и backend.');
  } finally {
    state.loadingAuth = false;
    render();
  }
}

async function loadClients() {
  const response = await apiFetch('/clients');
  if (!response.ok) return setMessage('Не удалось загрузить клиентов.');
  const data = await response.json();
  state.clients = data;
  if (!state.selectedClientId && data[0]) state.selectedClientId = String(data[0].id);
  render();
}

async function createClient() {
  const response = await apiFetch('/clients', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(state.createClient)
  });
  if (!response.ok) return setMessage('Ошибка создания клиента. Проверь обязательные поля.');
  state.createClient = { lastName: '', firstName: '', patronymic: '', phone: '', email: '', address: '' };
  await loadClients();
  setMessage('Клиент успешно создан.');
}

async function loadMaterials() {
  state.loadingMaterials = true;
  render();
  const response = await apiFetch('/materials');
  state.loadingMaterials = false;
  if (!response.ok) return setMessage('Материалы не загрузились.');
  state.materials = await response.json();
  render();
}

async function createCalculation() {
  const response = await apiFetch(`/calculations/client/${state.selectedClientId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ constructionAddress: state.address })
  });
  if (!response.ok) return setMessage('Ошибка создания расчёта.');
  const data = await response.json();
  state.calcId = data.id;
  render();
  setMessage(`Создан расчёт #${data.id}.`);
}

async function postFrame() {
  const payload = { floors: Number(state.floorCount), floorParamsList: Array.from({ length: Number(state.floorCount) }, () => state.frameFloor) };
  const response = await apiFetch(`/calculations/${state.calcId}/frame`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!response.ok) return setMessage('Ошибка расчёта каркаса.');
  const data = await response.json();
  setMessage(`Каркас рассчитан. Итого: ${data.totalCost} ₽`);
}

async function postFoundation() {
  const payload = {
    ...state.foundation,
    externalPerimeter: Number(state.foundation.externalPerimeter),
    innerWallLength: Number(state.foundation.innerWallLength)
  };
  const response = await apiFetch(`/calculations/${state.calcId}/foundation`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!response.ok) return setMessage('Ошибка расчёта фундамента.');
  const data = await response.json();
  setMessage(`Фундамент рассчитан. Итого: ${data.totalCost} ₽`);
}

async function loadCabinet() {
  if (!state.selectedClientId) return setMessage('Сначала выберите клиента.');
  const response = await apiFetch(`/calculations/client/${state.selectedClientId}`);
  if (!response.ok) return setMessage('Не удалось загрузить кабинет клиента.');
  state.cabinetCalcs = await response.json();
  render();
}

async function updateCalcStatus(calcId, status) {
  const response = await apiFetch(`/calculations/${calcId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status })
  });
  if (!response.ok) return setMessage('Не удалось обновить статус.');
  setMessage(`Статус #${calcId} обновлён: ${status}.`);
  await loadCabinet();
}

async function copyCalculation(calcId) {
  const response = await apiFetch(`/calculations/${calcId}/copy`, { method: 'POST' });
  if (!response.ok) return setMessage('Не удалось скопировать расчёт.');
  setMessage(`Расчёт #${calcId} скопирован.`);
  await loadCabinet();
}

async function deleteCalculation(calcId) {
  const response = await apiFetch(`/calculations/${calcId}`, { method: 'DELETE' });
  if (!response.ok) return setMessage('Не удалось удалить расчёт.');
  setMessage(`Расчёт #${calcId} удалён.`);
  await loadCabinet();
}

function applyHeroImage() {
  const hero = document.querySelector('.hero-bg');
  if (!hero) return;
  hero.classList.add('fade');
  setTimeout(() => {
    hero.style.backgroundImage = `linear-gradient(120deg, rgba(13,14,18,.86), rgba(13,14,18,.36)), url(${HERO_IMAGES[state.heroIndex]})`;
    hero.classList.remove('fade');
  }, 180);
}

function attachHandlers() {
  document.querySelectorAll('.tab-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const nextTab = btn.dataset.tab;
      if (!state.authenticated && nextTab !== 'home') {
        setMessage('Для доступа к разделу войдите в систему.');
        return;
      }
      state.tab = nextTab;
      render();
    });
  });

  document.getElementById('loginForm')?.addEventListener('submit', login);
  document.getElementById('emailInput')?.addEventListener('input', (e) => { state.email = e.target.value; });
  document.getElementById('passwordInput')?.addEventListener('input', (e) => { state.password = e.target.value; });
  document.getElementById('logoutBtn')?.addEventListener('click', () => logout());
  document.getElementById('refreshSessionBtn')?.addEventListener('click', async () => {
    const ok = await refreshSession();
    setMessage(ok ? 'Сессия обновлена.' : 'Не удалось обновить сессию.');
    render();
  });

  document.getElementById('loadClientsBtn')?.addEventListener('click', loadClients);
  document.getElementById('clientsSelect')?.addEventListener('change', (e) => { state.selectedClientId = e.target.value; });
  document.getElementById('addressInput')?.addEventListener('input', (e) => { state.address = e.target.value; });
  document.getElementById('createCalcBtn')?.addEventListener('click', createCalculation);
  document.getElementById('calcIdInput')?.addEventListener('input', (e) => { state.calcId = e.target.value; });
  document.getElementById('floorCountInput')?.addEventListener('input', (e) => { state.floorCount = e.target.value; });
  document.getElementById('floorHeightInput')?.addEventListener('input', (e) => { state.frameFloor.floorHeight = Number(e.target.value); });
  document.getElementById('perimeterInput')?.addEventListener('input', (e) => { state.frameFloor.perimeter = Number(e.target.value); });
  document.getElementById('postFrameBtn')?.addEventListener('click', postFrame);
  document.getElementById('foundationPerimeterInput')?.addEventListener('input', (e) => { state.foundation.externalPerimeter = e.target.value; });
  document.getElementById('foundationInnerInput')?.addEventListener('input', (e) => { state.foundation.innerWallLength = e.target.value; });
  document.getElementById('postFoundationBtn')?.addEventListener('click', postFoundation);

  document.getElementById('loadMaterialsBtn')?.addEventListener('click', loadMaterials);
  document.getElementById('materialFilterInput')?.addEventListener('input', (e) => { state.materialFilter = e.target.value; render(); });
  document.querySelectorAll('.add-cart-btn').forEach((btn) => btn.addEventListener('click', () => {
    const item = state.materials.find((m) => String(m.id) === btn.dataset.materialId);
    if (!item) return;
    state.cart.push(item);
    storage.set('cart', JSON.stringify(state.cart));
    setMessage(`${item.name} добавлен в корзину.`);
    render();
  }));

  document.getElementById('createClientBtn')?.addEventListener('click', createClient);
  document.querySelectorAll('[data-client-input]').forEach((input) => {
    input.addEventListener('input', (e) => {
      state.createClient[e.target.dataset.clientInput] = e.target.value;
    });
  });

  document.getElementById('loadCabinetBtn')?.addEventListener('click', loadCabinet);
  document.querySelectorAll('.status-btn').forEach((btn) => btn.addEventListener('click', () => updateCalcStatus(btn.dataset.calcId, btn.dataset.status)));
  document.querySelectorAll('.copy-btn').forEach((btn) => btn.addEventListener('click', () => copyCalculation(btn.dataset.calcId)));
  document.querySelectorAll('.delete-btn').forEach((btn) => btn.addEventListener('click', () => deleteCalculation(btn.dataset.calcId)));
  document.getElementById('clearCartBtn')?.addEventListener('click', () => {
    state.cart = [];
    storage.set('cart', '[]');
    render();
  });
}

function render() {
  const root = document.getElementById('root');
  const protectedLocked = !state.authenticated;

  root.innerHTML = `
    <header class="header glass">
      <div class="brand">BuildFlow</div>
      <nav>
        ${[['home', 'Главная'], ['calculator', 'Калькулятор'], ['materials', 'Материалы'], ['cabinet', 'Личный кабинет']]
          .map(([v, t]) => `<button class="tab-btn ${state.tab === v ? 'active' : ''}" data-tab="${v}">${t}</button>`).join('')}
      </nav>
      <div class="auth-chip">@${escapeHtml(state.authInfo)}</div>
    </header>

    <main>
      ${state.tab === 'home' ? `
      <section class="hero-shell">
        <div class="hero-bg" style="background-image: linear-gradient(120deg, rgba(13,14,18,.86), rgba(13,14,18,.36)), url(${HERO_IMAGES[state.heroIndex]})">
          <div class="hero-content fade-up">
            <p class="pill">BuildFlow Platform</p>
            <h1>Управляйте стройкой, сметой и клиентом в едином кабинете</h1>
            <p>Красивый интерфейс, локальные медиа, живой расчёт каркаса/фундамента и рабочая авторизация через backend JWT.</p>
          </div>
        </div>
      </section>
      <section class="media-grid">
        ${GALLERY_IMAGES.map((img) => `<article class="media-card"><img src="${img.src}" alt="${img.title}" /><p>${img.title}</p></article>`).join('')}
      </section>
      ` : ''}

      <section class="content-grid">
        <article class="card gradient-border">
          <h3>Авторизация</h3>
          <form class="login-form" id="loginForm">
            <input id="emailInput" value="${escapeHtml(state.email)}" placeholder="login" />
            <input id="passwordInput" type="password" value="${escapeHtml(state.password)}" placeholder="password" />
            <button type="submit">${state.loadingAuth ? 'Входим...' : 'Войти'}</button>
          </form>
          <div class="auth-actions">
            <button id="refreshSessionBtn" class="ghost" type="button">Обновить сессию</button>
            <button id="logoutBtn" class="ghost danger" type="button">Выйти</button>
          </div>
          <small>JWT: access + refresh</small>
        </article>

        <article class="card">
          <h3>Профиль</h3>
          <p>Пользователь: <strong>${escapeHtml(state.authInfo)}</strong></p>
          <p>Статус: <strong>${state.authenticated ? 'Авторизован' : 'Не авторизован'}</strong></p>
          <p>Корзина: <strong>${state.cart.length}</strong> позиций, сумма <strong>${totalCart().toFixed(2)} ₽</strong></p>
        </article>
      </section>

      ${state.tab === 'calculator' ? `
      <section class="content-grid wide ${protectedLocked ? 'locked' : ''}">
        ${protectedLocked ? '<div class="locked-note">Требуется вход в систему.</div>' : ''}
        <article class="card">
          <h3>Клиент и расчёт</h3>
          <div class="row">
            <button id="loadClientsBtn">Загрузить клиентов</button>
            <select id="clientsSelect">${state.clients.map((c) => `<option value="${c.id}" ${String(c.id)===state.selectedClientId?'selected':''}>${escapeHtml(`${c.lastName} ${c.firstName}`)}</option>`).join('')}</select>
          </div>
          <input id="addressInput" value="${escapeHtml(state.address)}" placeholder="Адрес строительства" />
          <button id="createCalcBtn">Создать расчёт</button>
          <input id="calcIdInput" value="${escapeHtml(String(state.calcId))}" placeholder="ID расчёта" />
        </article>

        <article class="card">
          <h3>Каркас</h3>
          <div class="row"><label>Этажей</label><input id="floorCountInput" type="number" min="1" max="5" value="${state.floorCount}" /></div>
          <div class="row"><label>Высота</label><input id="floorHeightInput" type="number" value="${state.frameFloor.floorHeight}" /></div>
          <div class="row"><label>Периметр</label><input id="perimeterInput" type="number" value="${state.frameFloor.perimeter}" /></div>
          <button id="postFrameBtn">Рассчитать каркас</button>
        </article>

        <article class="card">
          <h3>Фундамент</h3>
          <div class="row"><label>Периметр</label><input id="foundationPerimeterInput" type="number" value="${state.foundation.externalPerimeter}" /></div>
          <div class="row"><label>Внутр. стены</label><input id="foundationInnerInput" type="number" value="${state.foundation.innerWallLength}" /></div>
          <button id="postFoundationBtn">Рассчитать фундамент</button>
        </article>
      </section>` : ''}

      ${state.tab === 'materials' ? `
      <section class="content-grid wide ${protectedLocked ? 'locked' : ''}">
        ${protectedLocked ? '<div class="locked-note">Требуется вход в систему.</div>' : ''}
        <article class="card">
          <h3>Каталог материалов</h3>
          <div class="row">
            <button id="loadMaterialsBtn">${state.loadingMaterials ? 'Загрузка...' : 'Подтянуть из backend'}</button>
            <input id="materialFilterInput" value="${escapeHtml(state.materialFilter)}" placeholder="Поиск" />
          </div>
          <div class="material-list">
            ${filteredMaterials().map((m) => `<div class="material-item"><div><strong>${escapeHtml(m.name)}</strong><p>${escapeHtml(m.unit || '')}</p></div><div class="price-zone"><span>${m.currentPrice} ₽</span><button class="add-cart-btn" data-material-id="${m.id}">В корзину</button></div></div>`).join('')}
          </div>
        </article>
      </section>` : ''}

      ${state.tab === 'cabinet' ? `
      <section class="content-grid wide ${protectedLocked ? 'locked' : ''}">
        ${protectedLocked ? '<div class="locked-note">Требуется вход в систему.</div>' : ''}
        <article class="card">
          <h3>Новый клиент</h3>
          <div class="login-form">
            <input data-client-input="lastName" value="${escapeHtml(state.createClient.lastName)}" placeholder="Фамилия*" />
            <input data-client-input="firstName" value="${escapeHtml(state.createClient.firstName)}" placeholder="Имя*" />
            <input data-client-input="patronymic" value="${escapeHtml(state.createClient.patronymic)}" placeholder="Отчество" />
            <input data-client-input="phone" value="${escapeHtml(state.createClient.phone)}" placeholder="Телефон" />
            <input data-client-input="email" value="${escapeHtml(state.createClient.email)}" placeholder="Email" />
            <input data-client-input="address" value="${escapeHtml(state.createClient.address)}" placeholder="Адрес" />
            <button id="createClientBtn">Создать клиента</button>
          </div>
        </article>

        <article class="card">
          <h3>История расчётов</h3>
          <button id="loadCabinetBtn">Обновить</button>
          <div class="material-list">
            ${state.cabinetCalcs.map((calc) => `<div class="material-item stack"><div><strong>#${calc.id}</strong><p>${escapeHtml(calc.constructionAddress || '')}</p><p class="status">${escapeHtml(calc.status || '')}</p></div><div class="status-actions"><button class="status-btn" data-calc-id="${calc.id}" data-status="ACTUAL">ACTUAL</button><button class="status-btn" data-calc-id="${calc.id}" data-status="NOT_ACTUAL">NOT_ACTUAL</button><button class="copy-btn" data-calc-id="${calc.id}">Копия</button><button class="delete-btn danger" data-calc-id="${calc.id}">Удалить</button></div></div>`).join('')}
          </div>
        </article>
      </section>` : ''}

      <p class="result-msg">${escapeHtml(state.resultMessage)}</p>
    </main>
  `;

  attachHandlers();
}

async function bootstrap() {
  if (!state.authenticated && storage.get('refreshToken', '')) {
    const refreshed = await refreshSession();
    if (refreshed) state.authenticated = true;
  }

  if (state.authenticated) {
    await loadClients();
  }

  render();
  if (!state.heroTimer) {
    state.heroTimer = setInterval(() => {
      state.heroIndex = (state.heroIndex + 1) % HERO_IMAGES.length;
      applyHeroImage();
    }, 6000);
  }
}

bootstrap();
