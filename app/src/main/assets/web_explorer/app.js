// Linq Studio Web Explorer Engine
document.addEventListener('DOMContentLoaded', () => {
  const btnViewResult = document.getElementById('btnViewResult');
  const btnViewCode = document.getElementById('btnViewCode');
  const btnRunActive = document.getElementById('btnRunActive');
  const liveResultView = document.getElementById('liveResultView');
  const editorCodeBody = document.getElementById('editorCodeBody');
  const codeEditorArea = document.getElementById('codeEditorArea');
  const userRenderedContent = document.getElementById('userRenderedContent');
  const btnResultCounter = document.getElementById('btnResultCounter');
  const btnResultReset = document.getElementById('btnResultReset');
  const btnResultToggleTheme = document.getElementById('btnResultToggleTheme');
  const counterLabel = document.getElementById('counterLabel');
  const resultCanvas = document.getElementById('resultCanvas');
  const toastContainer = document.getElementById('toastContainer');

  let clicks = 0;
  if (btnResultCounter && counterLabel) {
    btnResultCounter.addEventListener('click', () => {
      clicks++;
      counterLabel.textContent = `Clicks: ${clicks}`;
    });
  }

  if (btnResultReset && counterLabel) {
    btnResultReset.addEventListener('click', () => {
      clicks = 0;
      counterLabel.textContent = 'Clicks: 0';
    });
  }

  function drawCanvas(color = '#38BDF8') {
    if (!resultCanvas) return;
    const ctx = resultCanvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, resultCanvas.width, resultCanvas.height);
    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.beginPath();
    const w = resultCanvas.width;
    const h = resultCanvas.height;
    for (let x = 0; x < w; x++) {
      const y = h / 2 + Math.sin(x * 0.04) * 20 * Math.cos(x * 0.015);
      if (x === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
  }

  if (btnResultToggleTheme) {
    const colors = ['#6366F1', '#06B6D4', '#10B981', '#F59E0B', '#EC4899'];
    let idx = 0;
    btnResultToggleTheme.addEventListener('click', () => {
      idx = (idx + 1) % colors.length;
      drawCanvas(colors[idx]);
    });
  }
  drawCanvas();

  function showToast(msg) {
    if (!toastContainer) return;
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = msg;
    toastContainer.appendChild(t);
    setTimeout(() => t.remove(), 2500);
  }

  function setView(mode) {
    if (mode === 'result') {
      btnViewResult.classList.add('active');
      btnViewCode.classList.remove('active');
      liveResultView.style.display = 'flex';
      editorCodeBody.style.display = 'none';
      drawCanvas();
    } else {
      btnViewCode.classList.add('active');
      btnViewResult.classList.remove('active');
      liveResultView.style.display = 'none';
      editorCodeBody.style.display = 'flex';
    }
  }

  if (btnViewResult) btnViewResult.addEventListener('click', () => setView('result'));
  if (btnViewCode) btnViewCode.addEventListener('click', () => setView('code'));

  if (btnRunActive) {
    btnRunActive.addEventListener('click', () => {
      const code = codeEditorArea ? codeEditorArea.value : '';
      if (userRenderedContent) {
        userRenderedContent.innerHTML = `<div style="background:#0F172A; border-radius:8px; padding:16px; border:1px solid #334155;">${code}</div>`;
      }
      setView('result');
      showToast('✓ Executed live code output!');
    });
  }

  // Default to Result View
  setView('result');
});
