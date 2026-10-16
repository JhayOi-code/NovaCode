/**
 * Prism.js Core with HTML (markup), CSS, and JavaScript definitions
 * Optimized for real-time typing in Linq Studio
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
      txt: {}
    },
    plugins: {},
    highlightAll: function(async, callback) {
      _.highlightAllUnder(document, async, callback);
    },
    highlightAllUnder: function(container, async, callback) {
      var env = {
        callback: callback,
        container: container,
        selector: 'code[class*="language-"], [class*="language-"] code, code[class*="lang-"], [class*="lang-"] code'
      };
      var elements = env.container.querySelectorAll(env.selector);
      for (var i = 0, element; element = elements[i++];) {
        _.highlightElement(element, async, env.callback);
      }
    },
    highlightElement: function(element, async, callback) {
      var language = element.className.match(lang);
      language = language ? language[1].toLowerCase() : 'none';
      var grammar = _.languages[language];
      if (!grammar) grammar = _.languages.javascript || _.languages.plain;
      element.className = element.className.replace(lang, '').replace(/\s+/g, ' ') + ' language-' + language;
      var code = element.textContent;
      var env = {
        element: element,
        language: language,
        grammar: grammar,
        code: code
      };
      env.highlightedCode = _.highlight(env.code, env.grammar, env.language);
      element.innerHTML = env.highlightedCode;
      if (callback) callback.call(element);
    },
    highlight: function(text, grammar, language) {
      var env = {
        text: text,
        grammar: grammar,
        language: language
      };
      env.tokens = _.tokenize(env.text, env.grammar);
      return Token.stringify(_.util.encode(env.tokens), env.language);
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
    if (typeof o == 'string') {
      return o;
    }
    if (Array.isArray(o)) {
      var s = '';
      o.forEach(function(e) { s += stringify(e, language); });
      return s;
    }
    var env = {
      type: o.type,
      content: stringify(o.content, language),
      tag: 'span',
      classes: ['token', o.type],
      attributes: {},
      language: language
    };
    var aliases = o.alias;
    if (aliases) {
      if (Array.isArray(aliases)) {
        Array.prototype.push.apply(env.classes, aliases);
      } else {
        env.classes.push(aliases);
      }
    }
    var attributes = '';
    for (var name in env.attributes) {
      attributes += ' ' + name + '="' + (env.attributes[name] || '').replace(/"/g, '&quot;') + '"';
    }
    return '<' + env.tag + ' class="' + env.classes.join(' ') + '"' + attributes + '>' + env.content + '</' + env.tag + '>';
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
        var pattern = patterns[j];
        var inside = pattern.inside;
        var lookbehind = !!pattern.lookbehind;
        var greedy = !!pattern.greedy;
        var patternObj = pattern.pattern || pattern;
        for (var currentNode = startNode.next, pos = startPos; currentNode !== tokenList.tail; pos += currentNode.value.length, currentNode = currentNode.next) {
          if (greedy && pos >= text.length) break;
          var str = currentNode.value;
          if (tokenList.length > text.length) break;
          if (str instanceof Token) continue;
          var match = matchPattern(patternObj, greedy ? pos : 0, str, lookbehind);
          if (!match) continue;
          var from = match.index;
          var matchStr = match[0];
          var before = str.slice(0, from);
          var after = str.slice(from + matchStr.length);
          var reach = pos + str.length;
          var removeFrom = currentNode.prev;
          if (before) {
            removeFrom = addAfter(tokenList, removeFrom, before);
            pos += before.length;
          }
          removeRange(tokenList, removeFrom, currentNode);
          var wrapped = new Token(token, inside ? _.tokenize(matchStr, inside) : matchStr, pattern.alias, matchStr);
          currentNode = addAfter(tokenList, removeFrom, wrapped);
          if (after) {
            addAfter(tokenList, currentNode, after);
          }
          if (from === 0) break;
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
  function removeRange(list, node, to) {
    var next = to.next;
    node.next = next;
    next.prev = node;
    list.length--;
  }
  function toArray(list) {
    var array = [];
    var node = list.head.next;
    while (node !== list.tail) {
      array.push(node.value);
      node = node.next;
    }
    return array;
  }

  return _;
})(_self);

// MARKUP / HTML DEFINITIONS
Prism.languages.markup = {
  'comment': /<!--[\s\S]*?-->/,
  'prolog': /<\?[\s\S]+?\?>/,
  'doctype': {
    pattern: /<!DOCTYPE(?:[^>"'[\]]|"[^"]*"|'[^']*')+(?:\[(?:[^<"'\]]|"[^"]*"|'[^']*'|<[^>]*>)*\]\s*)?>/i,
    greedy: true
  },
  'cdata': /<!\[CDATA\[[\s\S]*?\]\]>/i,
  'tag': {
    pattern: /<\/?(?!\d)[^\s>\/=$<%]+(?:\s(?:\s*[^\s>\/=]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+(?=[\s>]))|(?=[\s/>])))+)?\s*\/?>/,
    greedy: true,
    inside: {
      'tag': {
        pattern: /^<\/?[^\s>\/]+/,
        inside: {
          'punctuation': /^<\/?/,
          'namespace': /^[^\s>\/:]+:/
        }
      },
      'attr-value': {
        pattern: /=\s*(?:"[^"]*"|'[^']*'|[^\s'">=]+)/,
        inside: {
          'punctuation': [
            { pattern: /^=/, alias: 'attr-equals' },
            /"|'/
          ]
        }
      },
      'punctuation': /\/?>/,
      'attr-name': {
        pattern: /[^\s>\/]+/,
        inside: {
          'namespace': /^[^\s>\/:]+:/
        }
      }
    }
  },
  'entity': [
    { pattern: /&[\da-z]{1,8};/i, alias: 'named-entity' },
    /&#x?[\da-f]{1,8};/i
  ]
};
Prism.languages.html = Prism.languages.markup;
Prism.languages.xml = Prism.languages.markup;

// CSS DEFINITIONS
Prism.languages.css = {
  'comment': /\/\*[\s\S]*?\*\//,
  'atrule': {
    pattern: /@[\w-]+[\s\S]*?(?:;|(?=\s*\{))/,
    inside: { 'rule': /@[\w-]+/ }
  },
  'url': {
    pattern: /url\((?:(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1|.*?)\)/i,
    greedy: true
  },
  'selector': /[^{}\s][^{};]*?(?=\s*\{)/,
  'string': {
    pattern: /(["'])(?:\\(?:\r\n|[\s\S])|(?!\1)[^\\\r\n])*\1/,
    greedy: true
  },
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
