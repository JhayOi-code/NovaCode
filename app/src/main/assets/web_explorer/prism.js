/**
 * Prism.js Core with HTML (markup), CSS, JavaScript, Python, Markdown, JSON, SQL, Java, Kotlin, C/C++, Bash
 * Optimized for real-time typing and dynamic syntax highlighting in Linq Studio
 */
var _self = typeof window !== 'undefined' ? window : (typeof WorkerGlobalScope !== 'undefined' && self instanceof WorkerGlobalScope ? self : {});
var Prism = (function(_self){
  var lang = /(?:^|\s)lang(?:uage)?-([\w-]+)(?!\S)/i;
  var uniqueId = 0;

  var _ = _self.Prism = {
    manual: _self.Prism && _self.Prism.manual,
    disableWorkerMessageHandler: _self.Prism && _self.Prism.disableWorkerMessageHandler,
    util: {
      encode: function(tokens) {
        if (tokens instanceof Token) {
          return new Token(tokens.type, _.util.encode(tokens.content), tokens.alias);
        } else if (Array.isArray(tokens)) {
          return tokens.map(_.util.encode);
        } else {
          return tokens.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/\u00a0/g, ' ');
        }
      },
      type: function(o) {
        return Object.prototype.toString.call(o).slice(8, -1);
      },
      objId: function(obj) {
        if (!obj['__id']) {
          Object.defineProperty(obj, '__id', { value: ++uniqueId });
        }
        return obj['__id'];
      },
      clone: function deepClone(o, visited) {
        var clone, id;
        visited = visited || {};
        switch (_.util.type(o)) {
          case 'Object':
            id = _.util.objId(o);
            if (visited[id]) return visited[id];
            clone = {};
            visited[id] = clone;
            for (var key in o) {
              if (o.hasOwnProperty(key)) {
                clone[key] = deepClone(o[key], visited);
              }
            }
            return clone;
          case 'Array':
            id = _.util.objId(o);
            if (visited[id]) return visited[id];
            clone = [];
            visited[id] = clone;
            o.forEach(function(v, i) { clone[i] = deepClone(v, visited); });
            return clone;
          default:
            return o;
        }
      }
    },
    languages: {
      plain: {},
      plaintext: {},
      text: {},
      txt: {},
      extend: function(id, redef) {
        var lang = _.util.clone(_.languages[id]);
        for (var key in redef) {
          lang[key] = redef[key];
        }
        return lang;
      },
      insertBefore: function(inside, before, insert, root) {
        root = root || _.languages;
        var grammar = root[inside];
        var ret = {};
        for (var token in grammar) {
          if (grammar.hasOwnProperty(token)) {
            if (token === before) {
              for (var newToken in insert) {
                if (insert.hasOwnProperty(newToken)) {
                  ret[newToken] = insert[newToken];
                }
              }
            }
            if (!insert.hasOwnProperty(token)) {
              ret[token] = grammar[token];
            }
          }
        }
        var old = root[inside];
        root[inside] = ret;
        return ret;
      },
      DFS: function DFS(o, callback, type, visited) {
        visited = visited || {};
        var objId = _.util.objId;
        for (var i in o) {
          if (o.hasOwnProperty(i)) {
            callback.call(o, i, o[i], type || i);
            var property = o[i],
                propertyType = _.util.type(property);
            if (propertyType === 'Object' && !visited[objId(property)]) {
              visited[objId(property)] = true;
              DFS(property, callback, null, visited);
            } else if (propertyType === 'Array' && !visited[objId(property)]) {
              visited[objId(property)] = true;
              DFS(property, callback, i, visited);
            }
          }
        }
      }
    },
    plugins: {},
    highlightAll: function(async, callback) {
      _.highlightAllUnder(document, async, callback);
    },
    highlightAllUnder: function(container, async, callback) {
      var elements = container.querySelectorAll('code[class*="language-"], [class*="language-"] code, code[class*="lang-"], [class*="lang-"] code');
      for (var i = 0, element; element = elements[i++];) {
        _.highlightElement(element, async === true, callback);
      }
    },
    highlightElement: function(element, async, callback) {
      var language = element.className.match(lang);
      language = language ? language[1] : 'plain';
      var grammar = _.languages[language] || _.languages.plain;
      element.className = element.className.replace(lang, '').replace(/\s+/g, ' ') + ' language-' + language;
      var code = element.textContent;
      element.innerHTML = _.highlight(code, grammar, language);
      callback && callback.call(element);
    },
    highlight: function(text, grammar, language) {
      var tokens = _.tokenize(text, grammar);
      return Token.stringify(_.util.encode(tokens), language);
    },
    tokenize: function(text, grammar) {
      var rest = grammar.rest;
      if (rest) {
        for (var token in rest) {
          grammar[token] = rest[token];
        }
        delete grammar.rest;
      }
      var tokenList = new LinkedList();
      addAfter(tokenList, tokenList.head, text);
      matchGrammar(text, tokenList, grammar, tokenList.head, 0);
      return toArray(tokenList);
    }
  };

  function Token(type, content, alias, matchedStr) {
    this.type = type;
    this.content = content;
    this.alias = alias;
    this.length = (matchedStr || '').length | 0;
  }

  Token.stringify = function stringify(o, language) {
    if (typeof o === 'string') return o;
    if (Array.isArray(o)) {
      var s = '';
      for (var i = 0; i < o.length; i++) s += stringify(o[i], language);
      return s;
    }
    var classes = ['token', o.type];
    if (o.alias) {
      if (Array.isArray(o.alias)) classes.push.apply(classes, o.alias);
      else classes.push(o.alias);
    }
    return '<span class="' + classes.join(' ') + '">' + stringify(o.content, language) + '</span>';
  };

  function matchPattern(pattern, pos, text, lookbehind) {
    pattern.lastIndex = pos;
    var match = pattern.exec(text);
    if (match && lookbehind && match[1]) {
      var lookbehindLength = match[1].length;
      match.index += lookbehindLength;
      match[0] = match[0].slice(lookbehindLength);
    }
    return match;
  }

  function matchGrammar(text, tokenList, grammar, startNode, startPos) {
    for (var token in grammar) {
      if (!grammar.hasOwnProperty(token) || !grammar[token]) continue;
      var patterns = grammar[token];
      patterns = Array.isArray(patterns) ? patterns : [patterns];
      for (var j = 0; j < patterns.length; ++j) {
        var patternObj = patterns[j],
            inside = patternObj.inside,
            lookbehind = !!patternObj.lookbehind,
            greedy = !!patternObj.greedy,
            pattern = patternObj.pattern || patternObj;

        if (greedy && !pattern.global) {
          pattern = RegExp(pattern.source, (pattern.flags || '') + 'g');
        }

        for (var currentNode = startNode.next, pos = startPos; currentNode !== tokenList.tail; pos += currentNode.value.length, currentNode = currentNode.next) {
          if (tokenList.length > text.length) return;
          var str = currentNode.value;
          if (typeof str !== 'string') continue;

          var match;
          if (greedy) {
            match = matchPattern(pattern, pos, text, lookbehind);
            if (!match || match.index >= pos + str.length) continue;
            var from = match.index, to = match.index + match[0].length, p = pos;
            while (from >= p + currentNode.value.length) {
              p += currentNode.value.length;
              currentNode = currentNode.next;
            }
            pos = p;
            if (typeof currentNode.value !== 'string') continue;
            var deleteCount = 0;
            var next = currentNode;
            while (next !== tokenList.tail && (p < to || typeof next.value !== 'string')) {
              deleteCount++;
              p += next.value.length;
              next = next.next;
            }
            str = text.slice(pos, p);
            match.index -= pos;
          } else {
            match = matchPattern(pattern, 0, str, lookbehind);
            if (!match) continue;
          }

          var from = match.index,
              matchStr = match[0],
              before = str.slice(0, from),
              after = str.slice(from + matchStr.length);

          var removeFrom = currentNode.prev;
          if (before) {
            removeFrom = addAfter(tokenList, removeFrom, before);
            pos += before.length;
          }
          removeRange(tokenList, currentNode, deleteCount || 1);
          var wrapped = new Token(token, inside ? _.tokenize(matchStr, inside) : matchStr, patternObj.alias, matchStr);
          currentNode = addAfter(tokenList, removeFrom, wrapped);
          if (after) {
            addAfter(tokenList, currentNode, after);
          }
          if (deleteCount > 1) {
            matchGrammar(text, tokenList, grammar, currentNode, pos);
          }
        }
      }
    }
  }

  function LinkedList() {
    var head = { value: null, prev: null, next: null };
    var tail = { value: null, prev: head, next: null };
    head.next = tail;
    this.head = head;
    this.tail = tail;
    this.length = 0;
  }

  function addAfter(list, node, value) {
    var next = node.next;
    var newNode = { value: value, prev: node, next: next };
    node.next = newNode;
    next.prev = newNode;
    list.length++;
    return newNode;
  }

  function removeRange(list, node, count) {
    var next = node.next;
    for (var i = 0; i < count && next !== list.tail; i++) {
      next = next.next;
    }
    node.prev.next = next;
    next.prev = node.prev;
    list.length -= count;
  }

  function toArray(list) {
    var array = [], node = list.head.next;
    while (node !== list.tail) {
      array.push(node.value);
      node = node.next;
    }
    return array;
  }

  return _;
})(_self);

// HTML / MARKUP DEFINITIONS
Prism.languages.markup = {
  'comment': /<!--[\s\S]*?-->/,
  'prolog': /<\?[\s\S]*?\?>/,
  'doctype': {
    pattern: /<!DOCTYPE(?:[^>"'[\]]|"[^"]*"|'[^']*')+(?:\[(?:[^<"'\]]|"[^"]*"|'[^']*'|<[^>]*>)*\]\s*)?>/i,
    greedy: true
  },
  'cdata': /<!\[CDATA\[[\s\S]*?\]\]>/i,
  'tag': {
    pattern: /<\/?(?!\d)[^\s>\/=$<%]+(?:\s(?:\s*[^\s>\/=]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+(?=[\s>]))|(?=[\s/>])))+)?\s*\/?>/,
    greedy: true,
    inside: {
      'tag': { pattern: /^<\/?[^\s>\/]+/, inside: { 'punctuation': /^<\/?/, 'namespace': /^[^\s>\/:]+:/ } },
      'attr-value': { pattern: /=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+)/, inside: { 'punctuation': [{ pattern: /^=/, alias: 'attr-equals' }, /"|'/] } },
      'punctuation': /\/?>/,
      'attr-name': { pattern: /[^\s>\/]+/, inside: { 'namespace': /^[^\s>\/:]+:/ } }
    }
  },
  'entity': [ { pattern: /&[\da-z]{1,8};/i, alias: 'named-entity' }, /&#x?[\da-f]{1,8};/i ]
};
Prism.languages.html = Prism.languages.markup;
Prism.languages.xml = Prism.languages.markup;

// CSS DEFINITIONS
Prism.languages.css = {
  'comment': /\/\*[\s\S]*?\*\//,
  'atrule': { pattern: /@[\w-]+[\s\S]*?(?:;|(?=\s*\{))/, inside: { 'rule': /@[\w-]+/ } },
  'url': { pattern: /url\((?:(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1|.*?)\)/i, greedy: true },
  'selector': /[^{}\s][^{};]*?(?=\s*\{)/,
  'string': { pattern: /(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/, greedy: true },
  'property': /[-_a-z\xA0-\uFFFF][-\w\xA0-\uFFFF]*(?=\s*:)/i,
  'important': /!important\b/i,
  'function': /[-a-z0-9]+(?=\()/i,
  'punctuation': /[(){};:,]/
};

// JAVASCRIPT DEFINITIONS
Prism.languages.javascript = {
  'comment': [
    { pattern: /(^|[^\\])\/\*[\s\S]*?(?:\*\/|$)/, lookbehind: true },
    { pattern: /(^|[^\\:])\/\/.*/, lookbehind: true, greedy: true }
  ],
  'string': {
    pattern: /(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1|`(?:\\[\s\S]|\${(?:[^{}]|{(?:[^{}]|{[^}]*})*})+}|(?!\`)[^\\`])*`/,
    greedy: true
  },
  'class-name': {
    pattern: /(\b(?:class|interface|extends|implements|instanceof|new)\s+)[\w.\$]+/,
    lookbehind: true
  },
  'keyword': /\b(?:as|async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally|for|from|function|get|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|set|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)\b/,
  'boolean': /\b(?:true|false)\b/,
  'function': /#?[_$a-zA-Z\xA0-\uFFFF][$\w\xA0-\uFFFF]*(?=\s*(?:\.\s*(?:apply|bind|call)\s*)?\()/,
  'number': /\b(?:(?:0[xX](?:[\dA-Fa-f](?:_[\dA-Fa-f])?)+|0[bB](?:[01](?:_[01])?)+|0[oO](?:[0-7](?:_[0-7])?)+)n?|(?:\d(?:_\d)?)+n|NaN|Infinity)\b|(?:\b(?:\d(?:_\d)?)+\.?(?:\d(?:_\d)?)*|\B\.(?:\d(?:_\d)?)+)(?:[eE][+-]?(?:\d(?:_\d)?)+)?/,
  'operator': /--|\+\+|\*\*=?|=>|&&=?|\|\|=?|[!=]==|<<=?|>>>?=?|[-+*/%&|^!=<>]=?|\.{3}|\?\?=?|\?\.?|[~:]/,
  'punctuation': /[{}[\];(),.:]/
};
Prism.languages.js = Prism.languages.javascript;
Prism.languages.ts = Prism.languages.javascript;
Prism.languages.typescript = Prism.languages.javascript;

// PYTHON DEFINITIONS
Prism.languages.python = {
  'comment': { pattern: /(^|[^\\])#.*/, lookbehind: true, greedy: true },
  'string-interpolation': {
    pattern: /(?:f|fr|rf)(?:("""[\s\S]*?"""|'''[\s\S]*?''')|("|')(?:\\.|(?!\2)[^\\\r\n])*\2)/i,
    greedy: true,
    inside: {
      'interpolation': {
        pattern: /((?:^|[^{])(?:\{\{)*)\{(?!\{)(?:[^{}]|{[^{}]*})*\}/,
        lookbehind: true,
        inside: { 'punctuation': /^\{|\}$/ }
      },
      'string': /[\s\S]+/
    }
  },
  'triple-quoted-string': {
    pattern: /(?:[rub]|rb|br)?("""[\s\S]*?"""|'''[\s\S]*?''')/i,
    greedy: true,
    alias: 'string'
  },
  'string': {
    pattern: /(?:[rub]|rb|br)?("|')(?:\\.|(?!\1)[^\\\r\n])*\1/i,
    greedy: true
  },
  'decorator': {
    pattern: /(^\s*)@\w+(?:\.\w+)*/m,
    lookbehind: true,
    alias: 'annotation',
    inside: { 'punctuation': /\./ }
  },
  'function': {
    pattern: /(def\s+)[\w]+(?=\s*\()/i,
    lookbehind: true
  },
  'class-name': {
    pattern: /(class\s+)[\w]+/i,
    lookbehind: true
  },
  'keyword': /\b(?:and|as|assert|async|await|break|class|continue|def|del|elif|else|except|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|raise|return|try|while|with|yield)\b/,
  'builtin': /\b(?:abs|all|any|bin|bool|bytearray|bytes|callable|chr|classmethod|compile|complex|delattr|dict|dir|divmod|enumerate|eval|exec|filter|float|format|frozenset|getattr|globals|hasattr|hash|help|hex|id|input|int|isinstance|issubclass|iter|len|list|locals|map|max|memoryview|min|next|object|oct|open|ord|pow|print|property|range|repr|reversed|round|set|setattr|slice|sorted|staticmethod|str|sum|super|tuple|type|vars|zip)\b/,
  'boolean': /\b(?:True|False|None)\b/,
  'number': /\b0(?:b(?:_?[01])+|o(?:_?[0-7])+|x(?:_?[a-f0-9])+)\b|(?:\b\d+(?:_\d+)*(?:\.(?:\d+(?:_\d+)*)?)?|\B\.\d+(?:_\d+)*)(?:e[+-]?\d+(?:_\d+)*)?j?\b/i,
  'operator': /[-+%=]=?|!=|\*\*?=?|\/\/?=?|<[<=>]?|>[=>]?|[&|^~]/,
  'punctuation': /[{}[\];(),.:]/
};
Prism.languages.py = Prism.languages.python;

// MARKDOWN DEFINITIONS
Prism.languages.markdown = {
  'code': [
    { pattern: /(```[\s\S]*?```|~~~[\s\S]*?~~~)/, greedy: true },
    { pattern: /(`[^`\r\n]+`)/, greedy: true }
  ],
  'title': [
    { pattern: /^\s*#{1,6}\s+.+$/m, alias: 'header' },
    { pattern: /^(\S[^\r\n]*)\r?\n[=-]{2,}$/m, lookbehind: true, alias: 'header' }
  ],
  'bold': { pattern: /(\*\*|__)(?:(?!\1)[\s\S])+\1/, greedy: true },
  'italic': { pattern: /([*_])(?:(?!\1)[\s\S])+\1/, greedy: true },
  'blockquote': { pattern: /^\s*>.*$/m, alias: 'comment' },
  'url': { pattern: /!?\[[^\]]*\]\([^\)]+\)/, greedy: true },
  'list': { pattern: /^\s*([*+-]|\d+\.)\s+/m, alias: 'punctuation' },
  'hr': { pattern: /^\s*([*-_]){3,}\s*$/m, alias: 'punctuation' },
  'punctuation': /[\[\]\(\)!#*_>`\-+]/
};
Prism.languages.md = Prism.languages.markdown;

// JSON DEFINITIONS
Prism.languages.json = {
  'property': { pattern: /(^|[^\\])"(?:\\.|[^\\"\r\n])*"(?=\s*:)/, lookbehind: true, greedy: true },
  'string': { pattern: /(^|[^\\])"(?:\\.|[^\\"\r\n])*"(?!\s*:)/, lookbehind: true, greedy: true },
  'comment': { pattern: /\/\/.*|\/\*[\s\S]*?(?:\*\/|$)/, greedy: true },
  'number': /-?\b\d+(?:\.\d+)?(?:e[+-]?\d+)?\b/i,
  'punctuation': /[{}[\],:]/,
  'operator': /:/,
  'boolean': /\b(?:true|false)\b/,
  'null': { pattern: /\bnull\b/, alias: 'keyword' }
};

// SQL DEFINITIONS
Prism.languages.sql = {
  'comment': { pattern: /(^|[^\\])(?:--.*|\/\*[\s\S]*?\*\/)/, lookbehind: true, greedy: true },
  'string': { pattern: /(^|[^@\\])('(?:''|[^'\\])*'|"(?:""|[^"\\])*")/, lookbehind: true, greedy: true },
  'keyword': /\b(?:ACTION|ADD|ALTER|AND|AS|ASC|BEGIN|BETWEEN|BY|CASCADE|CASE|CHECK|COLUMN|COMMIT|CONSTRAINT|CREATE|CROSS|CURRENT_DATE|CURRENT_TIME|CURRENT_TIMESTAMP|DATABASE|DEFAULT|DELETE|DESC|DISTINCT|DROP|ELSE|END|EXISTS|FOREIGN|FROM|FULL|GROUP|HAVING|IN|INDEX|INNER|INSERT|INTO|IS|JOIN|KEY|LEFT|LIKE|LIMIT|NOT|NULL|ON|OR|ORDER|OUTER|PRIMARY|REFERENCES|RIGHT|ROLLBACK|SELECT|SET|TABLE|THEN|TO|TRANSACTION|UNION|UNIQUE|UPDATE|VALUES|VIEW|WHERE)\b/i,
  'boolean': /\b(?:TRUE|FALSE|NULL)\b/i,
  'number': /\b0x[\da-f]+\b|\b\d+(?:\.\d*)?(?:e[+-]?\d+)?/i,
  'operator': /[-+*\/=%^~]|&&?|\|\|?|!=?|<(?:=>?|<|>)?|>[>=]?/,
  'punctuation': /[;[\](),.!]/
};

// JAVA DEFINITIONS
Prism.languages.java = {
  'comment': [ { pattern: /(^|[^\\])\/\*[\s\S]*?(?:\*\/|$)/, lookbehind: true }, { pattern: /(^|[^\\:])\/\/.*/, lookbehind: true, greedy: true } ],
  'string': { pattern: /(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/, greedy: true },
  'class-name': { pattern: /(\b(?:class|interface|extends|implements|new|record)\s+)[\w.\$]+/, lookbehind: true },
  'keyword': /\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|non-sealed|null|open|opens|package|permits|private|protected|provides|public|record|requires|return|sealed|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield)\b/,
  'function': /\b\w+(?=\s*\()/,
  'number': /\b0[xX][\da-fA-F_]+[lL]?\b|\b\d[\d_]*(?:\.[\d_]+)?(?:[eE][+-]?[\d_]+)?[fFdDlL]?\b/,
  'operator': /--|\+\+|[-+*/%&|^!=<>]=?|&&|\|\||<<|>>|>>>/,
  'punctuation': /[{}[\];(),.:]/
};

// KOTLIN DEFINITIONS
Prism.languages.kotlin = {
  'comment': [ { pattern: /(^|[^\\])\/\*[\s\S]*?(?:\*\/|$)/, lookbehind: true }, { pattern: /(^|[^\\:])\/\/.*/, lookbehind: true, greedy: true } ],
  'string': { pattern: /("""[\s\S]*?"""|"(?:\\.|[^\\"\r\n])*")/, greedy: true },
  'keyword': /\b(?:abstract|actual|as|break|by|catch|class|companion|const|constructor|continue|crossinline|data|delegate|do|dynamic|else|enum|expect|external|false|field|file|final|finally|for|fun|get|header|if|impl|import|in|infix|init|inline|inner|interface|internal|is|it|lateinit|noinline|null|object|open|operator|out|override|package|param|private|property|protected|public|receiver|reified|return|sealed|set|setparam|super|suspend|tailrec|this|throw|true|try|typealias|typeof|val|var|vararg|when|where|while)\b/,
  'function': /\b\w+(?=\s*\()/,
  'number': /\b0[xX][\da-fA-F]+[uUL]?\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[uULfF]?\b/,
  'operator': /--|\+\+|[-+*/%&|^!=<>]=?|->|&&|\|\||\?:|\.\./,
  'punctuation': /[{}[\];(),.:]/
};
Prism.languages.kt = Prism.languages.kotlin;
