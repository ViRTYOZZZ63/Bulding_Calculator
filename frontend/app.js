const { useEffect, useMemo, useState } = React;

const API_BASE = 'http://localhost:8080/api';
const HERO_IMAGES = [
  'https://images.unsplash.com/photo-1536895058696-a69b1c7ba34f?auto=format&fit=crop&w=1800&q=80',
  'https://images.unsplash.com/photo-1541976844346-f18aeac57b06?auto=format&fit=crop&w=1800&q=80',
  'https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&w=1800&q=80'
];

const authHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
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

function App() {
  const [tab, setTab] = useState('home');
  const [heroIndex, setHeroIndex] = useState(0);
  const [email, setEmail] = useState('manager');
  const [password, setPassword] = useState('password');
  const [authInfo, setAuthInfo] = useState(localStorage.getItem('login') || 'guest');
  const [materials, setMaterials] = useState([]);
  const [loadingMaterials, setLoadingMaterials] = useState(false);
  const [clients, setClients] = useState([]);
  const [selectedClientId, setSelectedClientId] = useState('1');
  const [address, setAddress] = useState('г. Москва, ул. Строителей, 9');
  const [calcId, setCalcId] = useState('');
  const [foundation, setFoundation] = useState({
    externalPerimeter: 48,
    innerWallLength: 36,
    pileType: 'Бетонные сваи 200*200*3000',
    concreteGrade: 'М300 (В22.5)'
  });
  const [frameFloor, setFrameFloor] = useState(defaultFloor);
  const [floorCount, setFloorCount] = useState(1);
  const [resultMessage, setResultMessage] = useState('');
  const [cabinetCalcs, setCabinetCalcs] = useState([]);
  const [cart, setCart] = useState(JSON.parse(localStorage.getItem('cart') || '[]'));

  useEffect(() => {
    const timer = setInterval(() => setHeroIndex((prev) => (prev + 1) % HERO_IMAGES.length), 5500);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  const totalCart = useMemo(() => cart.reduce((sum, item) => sum + Number(item.currentPrice || 0), 0), [cart]);

  const login = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login: email, password })
      });
      if (!response.ok) throw new Error('Логин не прошёл');
      const data = await response.json();
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('login', data.login);
      setAuthInfo(data.login);
      setResultMessage('⚡ Вход успешный. Токен сохранён в localStorage.');
    } catch (err) {
      setResultMessage('Не удалось войти. Проверь backend и тестового пользователя.');
    }
  };

  const loadMaterials = async () => {
    setLoadingMaterials(true);
    try {
      const response = await fetch(`${API_BASE}/materials`, { headers: { ...authHeaders() } });
      if (!response.ok) throw new Error('Материалы не загружены');
      setMaterials(await response.json());
    } catch {
      setResultMessage('Материалы не загрузились. Возможно backend не запущен.');
    } finally {
      setLoadingMaterials(false);
    }
  };

  const loadClients = async () => {
    try {
      const response = await fetch(`${API_BASE}/clients`, { headers: { ...authHeaders() } });
      if (!response.ok) throw new Error('Клиенты не загружены');
      const data = await response.json();
      setClients(data);
      if (data[0]) setSelectedClientId(String(data[0].id));
    } catch {
      setResultMessage('Не удалось загрузить клиентов.');
    }
  };

  const createCalculation = async () => {
    try {
      const response = await fetch(`${API_BASE}/calculations/client/${selectedClientId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ constructionAddress: address })
      });
      if (!response.ok) throw new Error();
      const data = await response.json();
      setCalcId(data.id);
      setResultMessage(`Создали расчёт #${data.id}. Теперь можно добавить каркас/фундамент.`);
    } catch {
      setResultMessage('Ошибка создания расчёта.');
    }
  };

  const postFrame = async () => {
    const floorParamsList = Array.from({ length: Number(floorCount) }, () => frameFloor);
    const payload = { floors: Number(floorCount), floorParamsList };
    try {
      const response = await fetch(`${API_BASE}/calculations/${calcId}/frame`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify(payload)
      });
      if (!response.ok) throw new Error();
      const data = await response.json();
      setResultMessage(`Каркас рассчитан. Сумма: ${data.totalCost} ₽`);
    } catch {
      setResultMessage('Ошибка при расчёте каркаса. Проверь данные/авторизацию.');
    }
  };

  const postFoundation = async () => {
    try {
      const response = await fetch(`${API_BASE}/calculations/${calcId}/foundation`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({ ...foundation, externalPerimeter: Number(foundation.externalPerimeter), innerWallLength: Number(foundation.innerWallLength) })
      });
      if (!response.ok) throw new Error();
      const data = await response.json();
      setResultMessage(`Фундамент рассчитан. Сумма: ${data.totalCost} ₽`);
    } catch {
      setResultMessage('Ошибка при расчёте фундамента.');
    }
  };

  const loadCabinet = async () => {
    try {
      const response = await fetch(`${API_BASE}/calculations/client/${selectedClientId}`, { headers: { ...authHeaders() } });
      if (!response.ok) throw new Error();
      setCabinetCalcs(await response.json());
    } catch {
      setResultMessage('ЛК не загрузился. Проверь доступ.');
    }
  };

  const addToCart = (material) => {
    setCart((prev) => [...prev, material]);
  };

  return (
    <>
      <header className="header glass">
        <div className="brand">BuildFlow</div>
        <nav>
          {[
            ['home', 'Главная'],
            ['calculator', 'Калькулятор'],
            ['materials', 'База материалов'],
            ['cabinet', 'Личный кабинет']
          ].map(([value, title]) => (
            <button key={value} className={tab === value ? 'active' : ''} onClick={() => setTab(value)}>{title}</button>
          ))}
        </nav>
        <div className="auth-chip">@{authInfo}</div>
      </header>

      <main>
        {tab === 'home' && (
          <section className="hero" style={{ backgroundImage: `linear-gradient(120deg, rgba(13,14,18,.85), rgba(13,14,18,.3)), url(${HERO_IMAGES[heroIndex]})` }}>
            <div className="hero-content fade-up">
              <p className="pill">Gen-Z интерфейс + рабочая бизнес-логика</p>
              <h1>Калькулятор стройматериалов с личным кабинетом и умной корзиной</h1>
              <p>Считаем каркас и фундамент, сравниваем цены и держим историю расчётов клиента в одном месте.</p>
              <div className="hero-actions">
                <button onClick={() => setTab('calculator')}>Запустить расчёт</button>
                <button className="ghost" onClick={() => setTab('materials')}>Сравнить цены</button>
              </div>
            </div>
          </section>
        )}

        <section className="content-grid">
          <article className="card gradient-border">
            <h3>Вход в backend</h3>
            <form className="login-form" onSubmit={login}>
              <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="login" />
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" />
              <button type="submit">Войти</button>
            </form>
            <small>Подключено к /api/auth/login</small>
          </article>

          <article className="card warehouse-card">
            <h3>API складов / маркетплейсов</h3>
            <p>Открытых бесплатных API именно по стройматериалам почти нет. Реалистичный стек:</p>
            <ul>
              <li>2GIS Places API (free tier) — ближайшие строймагазины.</li>
              <li>OpenStreetMap Overpass API — геоданные складов и баз.</li>
              <li>Свой агрегатор цен из CSV/XML прайсов поставщиков.</li>
            </ul>
          </article>
        </section>

        {tab === 'calculator' && (
          <section className="content-grid wide">
            <article className="card">
              <h3>1) Клиент + расчёт</h3>
              <div className="row">
                <button onClick={loadClients}>Загрузить клиентов</button>
                <select value={selectedClientId} onChange={(e) => setSelectedClientId(e.target.value)}>
                  {clients.map((c) => <option key={c.id} value={c.id}>{c.lastName} {c.firstName}</option>)}
                </select>
              </div>
              <input value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Адрес строительства" />
              <button onClick={createCalculation}>Создать расчёт</button>
              <input value={calcId} onChange={(e) => setCalcId(e.target.value)} placeholder="ID расчёта" />
            </article>

            <article className="card">
              <h3>2) Каркас</h3>
              <div className="row"><label>Этажей</label><input type="number" min="1" max="5" value={floorCount} onChange={(e) => setFloorCount(e.target.value)} /></div>
              <div className="row"><label>Высота</label><input type="number" value={frameFloor.floorHeight} onChange={(e)=>setFrameFloor({...frameFloor,floorHeight:Number(e.target.value)})} /></div>
              <div className="row"><label>Периметр</label><input type="number" value={frameFloor.perimeter} onChange={(e)=>setFrameFloor({...frameFloor,perimeter:Number(e.target.value)})} /></div>
              <button onClick={postFrame}>Рассчитать каркас</button>
            </article>

            <article className="card">
              <h3>3) Фундамент</h3>
              <div className="row"><label>Периметр</label><input type="number" value={foundation.externalPerimeter} onChange={(e)=>setFoundation({...foundation,externalPerimeter:e.target.value})} /></div>
              <div className="row"><label>Внутр. стены</label><input type="number" value={foundation.innerWallLength} onChange={(e)=>setFoundation({...foundation,innerWallLength:e.target.value})} /></div>
              <button onClick={postFoundation}>Рассчитать фундамент</button>
            </article>
          </section>
        )}

        {tab === 'materials' && (
          <section className="content-grid wide">
            <article className="card">
              <h3>База материалов</h3>
              <button onClick={loadMaterials}>{loadingMaterials ? 'Загрузка...' : 'Подтянуть с backend'}</button>
              <div className="material-list">
                {materials.map((m) => (
                  <div key={m.id} className="material-item">
                    <div>
                      <strong>{m.name}</strong>
                      <p>{m.unit}</p>
                    </div>
                    <div className="price-zone">
                      <span>{m.currentPrice} ₽</span>
                      <button onClick={() => addToCart(m)}>В корзину</button>
                    </div>
                  </div>
                ))}
              </div>
            </article>
          </section>
        )}

        {tab === 'cabinet' && (
          <section className="content-grid wide">
            <article className="card">
              <h3>Личный кабинет</h3>
              <button onClick={loadCabinet}>Обновить историю расчётов</button>
              <div className="material-list">
                {cabinetCalcs.map((calc) => (
                  <div key={calc.id} className="material-item">
                    <div>
                      <strong>Расчёт #{calc.id}</strong>
                      <p>{calc.constructionAddress}</p>
                    </div>
                    <span className="status">{calc.status}</span>
                  </div>
                ))}
              </div>
            </article>

            <article className="card">
              <h3>Корзина</h3>
              <p>Позиций: {cart.length}</p>
              <p>Сумма: {totalCart.toFixed(2)} ₽</p>
              <button onClick={() => setCart([])}>Очистить</button>
            </article>
          </section>
        )}

        <p className="result-msg">{resultMessage}</p>
      </main>
    </>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
