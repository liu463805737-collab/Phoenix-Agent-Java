const MARKED_URL = 'https://cdn.jsdelivr.net/npm/marked@12.0.0/marked.min.js';
const ECHARTS_URL =
  'https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js';

function escapeForHtml(text: string): string {
  return text
    .replaceAll(/&/g, '&amp;')
    .replaceAll(/</g, '&lt;')
    .replaceAll(/>/g, '&gt;')
    .replaceAll(/"/g, '&quot;');
}

export function buildReportHtml(markdownContent: string): string {
  const escapedContent = escapeForHtml(markdownContent);

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>分析报告</title>
<script src="${MARKED_URL}"></script>
<script src="${ECHARTS_URL}"></script>
<style>
* { box-sizing: border-box; }
body { margin: 0; padding: 20px; background: #f3f4f6; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; color: #374151; line-height: 1.6; }
.container { max-width: 900px; margin: 0 auto; background: #fff; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
h1 { font-size: 2.25rem; font-weight: 800; color: #1e3a8a; margin-top: 0; margin-bottom: 1.5rem; border-bottom: 2px solid #e5e7eb; padding-bottom: 0.5rem; }
h2 { font-size: 1.5rem; font-weight: 700; color: #2563eb; margin-top: 2.5rem; margin-bottom: 1rem; border-left: 5px solid #2563eb; padding-left: 12px; }
h3 { font-size: 1.25rem; font-weight: 600; color: #1f2937; margin-top: 1.5rem; margin-bottom: 0.75rem; }
p { margin-bottom: 1rem; }
ul, ol { margin-bottom: 1rem; padding-left: 1.5rem; }
li { margin-bottom: 0.25rem; }
code { background: #f1f5f9; padding: 0.2rem 0.4rem; border-radius: 4px; font-size: 0.875em; color: #d946ef; font-family: monospace; }
pre { background: #1e293b; color: #f8fafc; padding: 1rem; border-radius: 8px; overflow-x: auto; }
pre code { background: transparent; color: inherit; padding: 0; }
table { width: 100%; border-collapse: collapse; margin: 1rem 0; font-size: 14px; }
thead { background: #f8fafc; }
th, td { padding: 10px 14px; text-align: left; border: 1px solid #e2e8f0; }
th { font-weight: 600; color: #1e293b; }
tbody tr:hover { background: #f1f5f9; }
blockquote { border-left: 4px solid #2563eb; margin: 1rem 0; padding: 0.5rem 1rem; background: #eef2ff; color: #1e293b; }
.chart-box { width: 100%; height: 450px; margin: 30px 0; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.chart-error { display: flex; align-items: center; justify-content: center; height: 100%; color: #ef4444; background: #fef2f2; border: 1px dashed #ef4444; border-radius: 8px; }
</style>
</head>
<body>
<div class="container">
<div id="raw-markdown" style="display:none;">${escapedContent}</div>
<div id="render-target" class="markdown-body"><p style="color:#999;">正在渲染报告...</p></div>
</div>
<script>
(function() {
  try {
    var rawDiv = document.getElementById('raw-markdown');
    if (!rawDiv) return;
    var rawText = rawDiv.innerText;

    if (typeof marked === 'undefined') {
      document.getElementById('render-target').innerHTML = '<p style="color:red;">Marked\u5E93\u52A0\u8F7D\u5931\u8D25\uFF0C\u8BF7\u68C0\u67E5\u7F51\u7EDC</p>';
      return;
    }

    var renderer = new marked.Renderer();
    renderer.code = function(code, language) {
      if (language === 'echarts') {
        var id = 'chart_' + Math.random().toString(36).substr(2, 9);
        return '<div id="' + id + '" class="chart-box" data-option="' + encodeURIComponent(code) + '"></div>';
      }
      return '<pre><code class="language-' + language + '">' + code.replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</code></pre>';
    };

    document.getElementById('render-target').innerHTML = marked.parse(rawText, { renderer: renderer });

    if (typeof echarts !== 'undefined') {
      document.querySelectorAll('.chart-box').forEach(function(box) {
        try {
          var code = decodeURIComponent(box.getAttribute('data-option'));
          var option = new Function('return ' + code)();
          var myChart = echarts.init(box);
          myChart.setOption(option);
          window.addEventListener('resize', function() { myChart.resize(); });
        } catch(e) {
          box.innerHTML = '<div class="chart-error"><b>\u56FE\u8868\u6E32\u67D3\u9519\u8BEF</b><br/>' + e.message + '</div>';
        }
      });
    }
  } catch(e) {
    document.getElementById('render-target').innerHTML = '<p style="color:red;">\u62A5\u544A\u6E32\u67D3\u5931\u8D25: ' + e.message + '</p>';
  }
})();
</script>
</body>
</html>`;
}
