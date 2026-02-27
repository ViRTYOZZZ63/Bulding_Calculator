const API_BASE = 'http://localhost:8080/api';
const HERO_IMAGES = ['./media/hero-1.svg', './media/hero-2.svg', './media/hero-3.svg'];

const KPI = [
  { label: 'Средняя скорость расчёта', value: 'до 35 сек' },
  { label: 'Поставщиков в каталоге', value: '120+' },
  { label: 'Экономия бюджета', value: 'до 18%' },
  { label: 'Точность спецификации', value: '95%' }
];

const WORKFLOW = [
  { title: 'Загрузить клиента', text: 'Подтягиваем базу клиентов из backend и фиксируем объект строительства.' },
  { title: 'Рассчитать конструктив', text: 'Формируем каркас и фундамент, получаем укрупнённую смету за минуты.' },
  { title: 'Сверить материалы', text: 'Сравниваем цены и переносим нужные позиции в корзину закупки.' }
];

const storage = {
  get(key, fallback) {
    try {
      const value = localStorage.getItem(key);
      return value === null ? fallback : value;
    } catch {
      return fallback;
    }
  },
  set(key, value) {
    try {
      localStorage.setItem(key, value);
    } catch {
      // ignore storage errors (private mode / disabled storage)
    }
  }
};

const defaultFloor = {
  floorHeight: 2.8,
  perimeter: 48,
  foundationArea: 120,
  extWallThickness: 'MM_150',
  innerWallLength: 36,
  intWallThickness: 'MM_100',
  extOsb: 'OSB 12 мм',
  extVapor: 'Ондутис',
  extWindBarrier: 'Ветро-влагозащитная мембрана Brane А',
  extInsulation: 'Эковер 150мм',
  intOsb: 'OSB 9 мм',
  ceilingThickness: 'MM_200',
  ceilingOsb: 'OSB 9 мм',
  ceilingVapor: 'Пароизоляция Axton (b)',
  ceilingWindBarrier: 'Гидро-ветрозащита Тип А',
  ceilingInsulation: 'Минеральная вата 200мм',
  windows: [{ width: 1.4, height: 1.4, count: 8 }],
  externalDoors: [{ width: 1, height: 2.1, count: 2 }],
  internalDoors: [{ width: 0.9, height: 2.1, count: 6 }]
};

const state = {
  tab: 'home',
  heroIndex: 0,
  email: 'manager',
  password: 'password',
  authInfo: storage.get('login', 'guest') || 'guest',
  materials: [],
  loadingMaterials: false,
  clients: [],
  selectedClientId: '1',
  address: 'г. Москва, ул. Строителей, 9',
  calcId: '',
  materialFilter: '',
  foundation: {
    externalPerimeter: 48,
    innerWallLength: 36,
    pileType: 'Бетонные сваи 200*200*3000',
    concreteGrade: 'М300 (В22.5)'
  },
  frameFloor: { ...defaultFloor },
  floorCount: 1,
  resultMessage: 'Добро пожаловать! Начни с авторизации или перейди в калькулятор.',
  cabinetCalcs: [],
  cart: JSON.parse(storage.get('cart', '[]'))
};

const authHeaders = () => {
  const token = storage.get('accessToken', '');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const escapeHtml = (str = '') => String(str)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#039;');

const totalCart = () => state.cart.reduce((sum, item) => sum + Number(item.currentPrice || 0), 0);
const filteredMaterials = () => {
  const query = state.materialFilter.trim().toLowerCase();
  if (!query) return state.materials;
  return state.materials.filter((m) => `${m.name} ${m.unit}`.toLowerCase().includes(query));
};

function render() {
  const root = document.getElementById('root');
  root.innerHTML = `
    <header class="header glass">
      <div class="brand">BuildFlow</div>
      <nav>
        ${[['home','Главная'],['calculator','Калькулятор'],['materials','База материалов'],['cabinet','Личный кабинет']].map(([v,t]) => `<button class="tab-btn ${state.tab===v?'active':''}" data-tab="${v}">${t}</button>`).join('')}
      </nav>
      <div class="auth-chip">@${escapeHtml(state.authInfo)}</div>
    </header>

    <main>
      ${state.tab === 'home' ? `
        <section class="hero" style="background-image: linear-gradient(120deg, rgba(13,14,18,.86), rgba(13,14,18,.36)), url(${HERO_IMAGES[state.heroIndex]})">
          <div class="hero-content fade-up">
            <p class="pill">Платформа управления стройкой</p>
            <h1>Калькулятор стройматериалов с продуманным UX и CRM-подходом</h1>
            <p>Считаем каркас и фундамент, сравниваем цены поставщиков и держим историю расчётов клиента в одном рабочем пространстве.</p>
            <div class="hero-actions">
              <button data-tab-target="calculator">Запустить расчёт</button>
              <button class="ghost" data-tab-target="materials">Сравнить цены</button>
            </div>
          </div>
        </section>
        <section class="kpi-grid">
          ${KPI.map((m) => `<article class="kpi-card"><p>${m.label}</p><strong>${m.value}</strong></article>`).join('')}
        </section>
      ` : ''}

      <section class="content-grid">
        <article class="card gradient-border">
          <h3>Вход в backend</h3>
          <form class="login-form" id="loginForm">
            <input id="emailInput" value="${escapeHtml(state.email)}" placeholder="login" />
            <input id="passwordInput" type="password" value="${escapeHtml(state.password)}" placeholder="password" />
            <button type="submit">Войти</button>
          </form>
          <small>Подключено к /api/auth/login</small>
        </article>

        <article class="card warehouse-card">
          <h3>Наполнение платформы</h3>
          <ul>${WORKFLOW.map((w) => `<li><strong>${w.title}:</strong> ${w.text}</li>`).join('')}</ul>
        </article>
      </section>

      ${state.tab === 'calculator' ? `
        <section class="content-grid wide">
          <article class="card">
            <h3>1) Клиент + расчёт</h3>
            <div class="row">
              <button id="loadClientsBtn">Загрузить клиентов</button>
              <select id="clientsSelect">${state.clients.map((c) => `<option value="${c.id}" ${String(c.id)===state.selectedClientId?'selected':''}>${escapeHtml(`${c.lastName} ${c.firstName}`)}</option>`).join('')}</select>
            </div>
            <input id="addressInput" value="${escapeHtml(state.address)}" placeholder="Адрес строительства" />
            <button id="createCalcBtn">Создать расчёт</button>
            <input id="calcIdInput" value="${escapeHtml(String(state.calcId))}" placeholder="ID расчёта" />
          </article>
          <article class="card">
            <h3>2) Каркас</h3>
            <div class="row"><label>Этажей</label><input id="floorCountInput" type="number" min="1" max="5" value="${state.floorCount}" /></div>
            <div class="row"><label>Высота</label><input id="floorHeightInput" type="number" value="${state.frameFloor.floorHeight}" /></div>
            <div class="row"><label>Периметр</label><input id="perimeterInput" type="number" value="${state.frameFloor.perimeter}" /></div>
            <button id="postFrameBtn">Рассчитать каркас</button>
          </article>
          <article class="card">
            <h3>3) Фундамент</h3>
            <div class="row"><label>Периметр</label><input id="foundationPerimeterInput" type="number" value="${state.foundation.externalPerimeter}" /></div>
            <div class="row"><label>Внутр. стены</label><input id="foundationInnerInput" type="number" value="${state.foundation.innerWallLength}" /></div>
            <button id="postFoundationBtn">Рассчитать фундамент</button>
          </article>
        </section>
      ` : ''}

      ${state.tab === 'materials' ? `
        <section class="content-grid wide">
          <article class="card">
            <h3>База материалов</h3>
            <div class="row">
              <button id="loadMaterialsBtn">${state.loadingMaterials ? 'Загрузка...' : 'Подтянуть с backend'}</button>
              <input id="materialFilterInput" value="${escapeHtml(state.materialFilter)}" placeholder="Поиск по названию" />
            </div>
            <div class="material-list">
              ${filteredMaterials().map((m) => `
                <div class="material-item">
                  <div><strong>${escapeHtml(m.name)}</strong><p>${escapeHtml(m.unit || '')}</p></div>
                  <div class="price-zone"><span>${m.currentPrice} ₽</span><button class="add-cart-btn" data-material-id="${m.id}">В корзину</button></div>
                </div>`).join('')}
              ${filteredMaterials().length ? '' : '<p class="muted-note">По этому запросу ничего не найдено.</p>'}
            </div>
          </article>
        </section>
      ` : ''}

      ${state.tab === 'cabinet' ? `
        <section class="content-grid wide">
          <article class="card">
            <h3>Личный кабинет</h3>
            <button id="loadCabinetBtn">Обновить историю расчётов</button>
            <div class="material-list">
              ${state.cabinetCalcs.map((calc) => `<div class="material-item"><div><strong>Расчёт #${calc.id}</strong><p>${escapeHtml(calc.constructionAddress || '')}</p></div><span class="status">${escapeHtml(calc.status || '')}</span></div>`).join('')}
            </div>
          </article>
          <article class="card">
            <h3>Корзина закупки</h3>
            <p>Позиций: ${state.cart.length}</p>
            <p>Сумма: ${totalCart().toFixed(2)} ₽</p>
            <button id="clearCartBtn">Очистить</button>
          </article>
        </section>
      ` : ''}

      <p class="result-msg">${escapeHtml(state.resultMessage)}</p>
    </main>
  `;

  bindHandlers();
}

function safeRender() {
  try {
    render();
  } catch (error) {
    const root = document.getElementById('root');
    if (root) {
      root.innerHTML = '<main style="padding:24px;color:#fff"><h2>Ошибка рендера интерфейса</h2><p>Открой DevTools Console и пришли ошибку разработчику.</p></main>';
    }
    console.error(error);
  }
}

function setMessage(msg) { state.resultMessage = msg; safeRender(); }
function switchTab(tab) { state.tab = tab; safeRender(); }

async function login(e) {
  e.preventDefault();
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
    setMessage('⚡ Вход успешный. Токен сохранён в localStorage.');
  } catch {
    setMessage('Не удалось войти. Проверь backend и тестового пользователя.');
  }
}

async function loadMaterials() {
  state.loadingMaterials = true;
  safeRender();
  try {
    const response = await fetch(`${API_BASE}/materials`, { headers: { ...authHeaders() } });
    if (!response.ok) throw new Error();
    state.materials = await response.json();
    setMessage('Справочник материалов обновлён.');
  } catch {
    setMessage('Материалы не загрузились. Возможно backend не запущен.');
  } finally {
    state.loadingMaterials = false;
    safeRender();
  }
}

async function loadClients() {
  try {
    const response = await fetch(`${API_BASE}/clients`, { headers: { ...authHeaders() } });
    if (!response.ok) throw new Error();
    const data = await response.json();
    state.clients = data;
    if (data[0]) state.selectedClientId = String(data[0].id);
    setMessage(`Загружено клиентов: ${data.length}.`);
  } catch {
    setMessage('Не удалось загрузить клиентов.');
  }
}

async function createCalculation() {
  try {
    const response = await fetch(`${API_BASE}/calculations/client/${state.selectedClientId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify({ constructionAddress: state.address })
    });
    if (!response.ok) throw new Error();
    const data = await response.json();
    state.calcId = data.id;
    setMessage(`Создали расчёт #${data.id}. Теперь можно добавить каркас/фундамент.`);
  } catch {
    setMessage('Ошибка создания расчёта.');
  }
}

async function postFrame() {
  const floorParamsList = Array.from({ length: Number(state.floorCount) }, () => state.frameFloor);
  const payload = { floors: Number(state.floorCount), floorParamsList };
  try {
    const response = await fetch(`${API_BASE}/calculations/${state.calcId}/frame`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error();
    const data = await response.json();
    setMessage(`Каркас рассчитан. Сумма: ${data.totalCost} ₽`);
  } catch {
    setMessage('Ошибка при расчёте каркаса. Проверь данные/авторизацию.');
  }
}

async function postFoundation() {
  try {
    const response = await fetch(`${API_BASE}/calculations/${state.calcId}/foundation`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify({ ...state.foundation, externalPerimeter: Number(state.foundation.externalPerimeter), innerWallLength: Number(state.foundation.innerWallLength) })
    });
    if (!response.ok) throw new Error();
    const data = await response.json();
    setMessage(`Фундамент рассчитан. Сумма: ${data.totalCost} ₽`);
  } catch {
    setMessage('Ошибка при расчёте фундамента.');
  }
}

async function loadCabinet() {
  try {
    const response = await fetch(`${API_BASE}/calculations/client/${state.selectedClientId}`, { headers: { ...authHeaders() } });
    if (!response.ok) throw new Error();
    state.cabinetCalcs = await response.json();
    setMessage('История расчётов обновлена.');
  } catch {
    setMessage('ЛК не загрузился. Проверь доступ.');
  }
}

function bindHandlers() {
  document.querySelectorAll('.tab-btn').forEach((btn) => btn.addEventListener('click', () => switchTab(btn.dataset.tab)));
  document.querySelectorAll('[data-tab-target]').forEach((btn) => btn.addEventListener('click', () => switchTab(btn.dataset.tabTarget)));

  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', login);
    document.getElementById('emailInput').addEventListener('input', (e) => { state.email = e.target.value; });
    document.getElementById('passwordInput').addEventListener('input', (e) => { state.password = e.target.value; });
  }

  const loadClientsBtn = document.getElementById('loadClientsBtn');
  if (loadClientsBtn) {
    loadClientsBtn.addEventListener('click', loadClients);
    document.getElementById('clientsSelect').addEventListener('change', (e) => { state.selectedClientId = e.target.value; });
    document.getElementById('addressInput').addEventListener('input', (e) => { state.address = e.target.value; });
    document.getElementById('createCalcBtn').addEventListener('click', createCalculation);
    document.getElementById('calcIdInput').addEventListener('input', (e) => { state.calcId = e.target.value; });
    document.getElementById('floorCountInput').addEventListener('input', (e) => { state.floorCount = e.target.value; });
    document.getElementById('floorHeightInput').addEventListener('input', (e) => { state.frameFloor.floorHeight = Number(e.target.value); });
    document.getElementById('perimeterInput').addEventListener('input', (e) => { state.frameFloor.perimeter = Number(e.target.value); });
    document.getElementById('postFrameBtn').addEventListener('click', postFrame);
    document.getElementById('foundationPerimeterInput').addEventListener('input', (e) => { state.foundation.externalPerimeter = e.target.value; });
    document.getElementById('foundationInnerInput').addEventListener('input', (e) => { state.foundation.innerWallLength = e.target.value; });
    document.getElementById('postFoundationBtn').addEventListener('click', postFoundation);
  }

  const loadMaterialsBtn = document.getElementById('loadMaterialsBtn');
  if (loadMaterialsBtn) {
    loadMaterialsBtn.addEventListener('click', loadMaterials);
    document.getElementById('materialFilterInput').addEventListener('input', (e) => {
      state.materialFilter = e.target.value;
      safeRender();
    });
    document.querySelectorAll('.add-cart-btn').forEach((btn) => btn.addEventListener('click', () => {
      const material = state.materials.find((m) => String(m.id) === btn.dataset.materialId);
      if (!material) return;
      state.cart.push(material);
      storage.set('cart', JSON.stringify(state.cart));
      setMessage(`Добавили в корзину: ${material.name}.`);
    }));
  }

  const loadCabinetBtn = document.getElementById('loadCabinetBtn');
  if (loadCabinetBtn) {
    loadCabinetBtn.addEventListener('click', loadCabinet);
    document.getElementById('clearCartBtn').addEventListener('click', () => {
      state.cart = [];
      storage.set('cart', JSON.stringify(state.cart));
      safeRender();
    });
  }
}

setInterval(() => {
  state.heroIndex = (state.heroIndex + 1) % HERO_IMAGES.length;
  if (state.tab === 'home') safeRender();
}, 5500);

safeRender();
