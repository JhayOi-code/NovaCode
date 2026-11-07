/**
 * Linq Studio - Unified AI Provider Adapter Engine
 * Compliant with Part 1 specifications:
 * - 14+ AI Providers (OpenAI, Groq, OpenRouter, Anthropic, Gemini, Mistral, DeepSeek, xAI, Together, Fireworks, Perplexity, Cohere, Ollama, LM Studio, Custom)
 * - Live model discovery with 30-minute caching
 * - Complete eradication of "custom-model" / empty placeholder models
 * - Single-line sanitized API key management with masked display
 * - Stream parsing (OpenAI SSE, Anthropic events, Gemini SSE)
 * - AbortController cancellation & automatic non-streaming fallback
 * - Human-readable error parsing with collapsible details
 * - Browser direct-call with automatic secure relay fallback
 * - Device-only key persistence (localStorage, never synced)
 */

(function (root, factory) {
  if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.LinqAI = factory();
  }
}(typeof self !== 'undefined' ? self : this, function () {

  const STORAGE_KEYS = {
    KEYS: 'linq_ai_keys',
    SETTINGS: 'linq_ai_settings',
    MODELS_CACHE_PREFIX: 'linq_ai_models_'
  };

  const CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes

  // Known trusted hostnames for SSRF protection
  const KNOWN_AI_HOSTS = [
    'api.openai.com',
    'api.anthropic.com',
    'generativelanguage.googleapis.com',
    'api.groq.com',
    'openrouter.ai',
    'api.mistral.ai',
    'api.deepseek.com',
    'api.x.ai',
    'api.together.xyz',
    'api.fireworks.ai',
    'api.perplexity.ai',
    'api.cohere.com'
  ];

  // Provider Adapters Registry
  const PROVIDERS = {
    openai: {
      id: 'openai',
      name: 'OpenAI',
      baseUrl: 'https://api.openai.com',
      suggestedDefaultModel: 'gpt-4o-mini',
      keyPrefixes: ['sk-proj-', 'sk-'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.openai.com'}/v1/models`;
        const res = await fetchDirectOrRelay('openai', url, {
          method: 'GET',
          headers: PROVIDERS.openai.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('openai', res.status, data);
        const list = Array.isArray(data.data) ? data.data : [];
        return list
          .filter(m => m.id && (m.id.startsWith('gpt') || m.id.startsWith('o1') || m.id.startsWith('o3') || m.id.includes('chat')))
          .map(m => m.id)
          .sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.openai.com'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('openai', messages),
          stream: !!stream
        };
        // Newer OpenAI models prefer max_completion_tokens
        if (model.startsWith('o1') || model.startsWith('o3') || model.startsWith('gpt-4o')) {
          if (maxTokens) body.max_completion_tokens = maxTokens;
        } else if (maxTokens) {
          body.max_tokens = maxTokens;
        }
        return executeChatRequest('openai', url, PROVIDERS.openai.headers(key), body, stream, signal, key);
      }
    },

    groq: {
      id: 'groq',
      name: 'Groq',
      baseUrl: 'https://api.groq.com/openai',
      suggestedDefaultModel: 'llama-3.3-70b-versatile',
      keyPrefixes: ['gsk_'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.groq.com/openai'}/v1/models`;
        const res = await fetchDirectOrRelay('groq', url, {
          method: 'GET',
          headers: PROVIDERS.groq.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('groq', res.status, data);
        const list = Array.isArray(data.data) ? data.data : [];
        return list.map(m => m.id).filter(id => !id.includes('whisper')).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.groq.com/openai'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('groq', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('groq', url, PROVIDERS.groq.headers(key), body, stream, signal, key);
      }
    },

    openrouter: {
      id: 'openrouter',
      name: 'OpenRouter',
      baseUrl: 'https://openrouter.ai/api',
      suggestedDefaultModel: 'meta-llama/llama-3.2-3b-instruct:free',
      keyPrefixes: ['sk-or-'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': typeof window !== 'undefined' ? window.location.origin : 'https://linqstudio.local',
        'X-Title': 'Linq Studio IDE'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://openrouter.ai/api'}/v1/models`;
        const res = await fetchDirectOrRelay('openrouter', url, {
          method: 'GET',
          headers: PROVIDERS.openrouter.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('openrouter', res.status, data);
        const list = Array.isArray(data.data) ? data.data : [];
        return list.map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://openrouter.ai/api'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('openrouter', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('openrouter', url, PROVIDERS.openrouter.headers(key), body, stream, signal, key);
      }
    },

    anthropic: {
      id: 'anthropic',
      name: 'Anthropic',
      baseUrl: 'https://api.anthropic.com',
      suggestedDefaultModel: 'claude-3-5-sonnet-20241022',
      keyPrefixes: ['sk-ant-'],
      requiresRelayByDefault: true, // Anthropic blocks standard direct browser calls by CORS unless dangerous header used
      headers: (key) => ({
        'x-api-key': key,
        'anthropic-version': '2023-06-01',
        'anthropic-dangerous-direct-browser-access': 'true',
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.anthropic.com'}/v1/models`;
        const res = await fetchDirectOrRelay('anthropic', url, {
          method: 'GET',
          headers: PROVIDERS.anthropic.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('anthropic', res.status, data);
        const list = Array.isArray(data.data) ? data.data : [];
        return list.map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.anthropic.com'}/v1/messages`;
        
        let systemPrompt = '';
        const anthropicMessages = [];
        messages.forEach(m => {
          if (m.role === 'system') {
            systemPrompt += (systemPrompt ? '\n\n' : '') + m.content;
          } else {
            // Strictly alternating roles
            const role = m.role === 'assistant' ? 'assistant' : 'user';
            if (anthropicMessages.length > 0 && anthropicMessages[anthropicMessages.length - 1].role === role) {
              anthropicMessages[anthropicMessages.length - 1].content += '\n' + m.content;
            } else {
              anthropicMessages.push({ role, content: m.content || '...' });
            }
          }
        });

        if (anthropicMessages.length === 0) {
          anthropicMessages.push({ role: 'user', content: 'Hello' });
        }

        const body = {
          model,
          max_tokens: maxTokens || 1024,
          messages: anthropicMessages,
          stream: !!stream
        };
        if (systemPrompt) body.system = systemPrompt;

        return executeChatRequest('anthropic', url, PROVIDERS.anthropic.headers(key), body, stream, signal, key);
      }
    },

    gemini: {
      id: 'gemini',
      name: 'Google Gemini',
      baseUrl: 'https://generativelanguage.googleapis.com',
      suggestedDefaultModel: 'gemini-1.5-flash',
      keyPrefixes: ['AIza'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'x-goog-api-key': key, // STRICTLY in header, NEVER in URL query
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://generativelanguage.googleapis.com'}/v1beta/models`;
        const res = await fetchDirectOrRelay('gemini', url, {
          method: 'GET',
          headers: PROVIDERS.gemini.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('gemini', res.status, data);
        const list = Array.isArray(data.models) ? data.models : [];
        return list
          .filter(m => m.supportedGenerationMethods && m.supportedGenerationMethods.includes('generateContent'))
          .map(m => m.name.replace(/^models\//, ''))
          .filter(id => !id.includes('embedding') && !id.includes('aqa'))
          .sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const cleanModel = model.replace(/^models\//, '');
        const mode = stream ? 'streamGenerateContent?alt=sse' : 'generateContent';
        const url = `${baseUrl || 'https://generativelanguage.googleapis.com'}/v1beta/models/${cleanModel}:${mode}`;

        let systemInstruction = null;
        const contents = [];

        messages.forEach(m => {
          if (m.role === 'system') {
            systemInstruction = { parts: [{ text: m.content }] };
          } else {
            contents.push({
              role: m.role === 'assistant' ? 'model' : 'user',
              parts: [{ text: m.content || ' ' }]
            });
          }
        });

        const body = { contents };
        if (systemInstruction) body.systemInstruction = systemInstruction;
        if (maxTokens) body.generationConfig = { maxOutputTokens: maxTokens };

        return executeChatRequest('gemini', url, PROVIDERS.gemini.headers(key), body, stream, signal, key);
      }
    },

    mistral: {
      id: 'mistral',
      name: 'Mistral',
      baseUrl: 'https://api.mistral.ai',
      suggestedDefaultModel: 'mistral-small-latest',
      keyPrefixes: [],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.mistral.ai'}/v1/models`;
        const res = await fetchDirectOrRelay('mistral', url, {
          method: 'GET',
          headers: PROVIDERS.mistral.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('mistral', res.status, data);
        return (data.data || []).map(m => m.id).filter(id => !id.includes('embed')).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.mistral.ai'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('mistral', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('mistral', url, PROVIDERS.mistral.headers(key), body, stream, signal, key);
      }
    },

    deepseek: {
      id: 'deepseek',
      name: 'DeepSeek',
      baseUrl: 'https://api.deepseek.com',
      suggestedDefaultModel: 'deepseek-chat',
      keyPrefixes: ['dsk-'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.deepseek.com'}/models`;
        const res = await fetchDirectOrRelay('deepseek', url, {
          method: 'GET',
          headers: PROVIDERS.deepseek.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('deepseek', res.status, data);
        return (data.data || []).map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.deepseek.com'}/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('deepseek', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('deepseek', url, PROVIDERS.deepseek.headers(key), body, stream, signal, key);
      }
    },

    xai: {
      id: 'xai',
      name: 'xAI (Grok)',
      baseUrl: 'https://api.x.ai',
      suggestedDefaultModel: 'grok-2-latest',
      keyPrefixes: ['xai-'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.x.ai'}/v1/models`;
        const res = await fetchDirectOrRelay('xai', url, {
          method: 'GET',
          headers: PROVIDERS.xai.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('xai', res.status, data);
        return (data.data || []).map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.x.ai'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('xai', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('xai', url, PROVIDERS.xai.headers(key), body, stream, signal, key);
      }
    },

    together: {
      id: 'together',
      name: 'Together AI',
      baseUrl: 'https://api.together.xyz',
      suggestedDefaultModel: 'meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo',
      keyPrefixes: [],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.together.xyz'}/v1/models`;
        const res = await fetchDirectOrRelay('together', url, {
          method: 'GET',
          headers: PROVIDERS.together.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('together', res.status, data);
        const list = Array.isArray(data) ? data : (data.data || []);
        return list.filter(m => m.type === 'chat' || !m.type).map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.together.xyz'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('together', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('together', url, PROVIDERS.together.headers(key), body, stream, signal, key);
      }
    },

    fireworks: {
      id: 'fireworks',
      name: 'Fireworks AI',
      baseUrl: 'https://api.fireworks.ai/inference',
      suggestedDefaultModel: 'accounts/fireworks/models/llama-v3p1-8b-instruct',
      keyPrefixes: ['fw_'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.fireworks.ai/inference'}/v1/models`;
        const res = await fetchDirectOrRelay('fireworks', url, {
          method: 'GET',
          headers: PROVIDERS.fireworks.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('fireworks', res.status, data);
        return (data.data || []).map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.fireworks.ai/inference'}/v1/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('fireworks', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('fireworks', url, PROVIDERS.fireworks.headers(key), body, stream, signal, key);
      }
    },

    perplexity: {
      id: 'perplexity',
      name: 'Perplexity',
      baseUrl: 'https://api.perplexity.ai',
      suggestedDefaultModel: 'sonar',
      keyPrefixes: ['pplx-'],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        // Perplexity models list
        return ['sonar', 'sonar-pro', 'sonar-reasoning', 'sonar-reasoning-pro'];
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.perplexity.ai'}/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('perplexity', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('perplexity', url, PROVIDERS.perplexity.headers(key), body, stream, signal, key);
      }
    },

    cohere: {
      id: 'cohere',
      name: 'Cohere',
      baseUrl: 'https://api.cohere.com',
      suggestedDefaultModel: 'command-r',
      keyPrefixes: [],
      requiresRelayByDefault: false,
      headers: (key) => ({
        'Authorization': `Bearer ${key}`,
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const url = `${baseUrl || 'https://api.cohere.com'}/v1/models`;
        const res = await fetchDirectOrRelay('cohere', url, {
          method: 'GET',
          headers: PROVIDERS.cohere.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('cohere', res.status, data);
        const list = Array.isArray(data.models) ? data.models : [];
        return list.filter(m => m.endpoints && m.endpoints.includes('chat')).map(m => m.name).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        validateModel(model);
        const url = `${baseUrl || 'https://api.cohere.com'}/v2/chat`;
        const cohereMessages = messages.map(m => ({
          role: m.role === 'system' ? 'system' : (m.role === 'assistant' ? 'assistant' : 'user'),
          content: { type: 'text', text: m.content || ' ' }
        }));
        const body = {
          model,
          messages: cohereMessages,
          stream: !!stream
        };
        return executeChatRequest('cohere', url, PROVIDERS.cohere.headers(key), body, stream, signal, key);
      }
    },

    ollama: {
      id: 'ollama',
      name: 'Ollama (Local)',
      baseUrl: 'http://localhost:11434',
      suggestedDefaultModel: 'llama3',
      isLocal: true,
      headers: () => ({
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const base = baseUrl || 'http://localhost:11434';
        const res = await fetch(`${base}/api/tags`);
        if (!res.ok) throw parseError('ollama', res.status, await res.text());
        const data = await res.json();
        return (data.models || []).map(m => m.name).sort();
      },
      chat: async ({ messages, model, stream, signal, baseUrl }) => {
        validateModel(model);
        const base = baseUrl || 'http://localhost:11434';
        const body = {
          model,
          messages: messages.map(m => ({ role: m.role, content: m.content })),
          stream: !!stream
        };
        const res = await fetch(`${base}/api/chat`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body),
          signal
        });
        if (!res.ok) throw parseError('ollama', res.status, await res.text());
        return res;
      }
    },

    lmstudio: {
      id: 'lmstudio',
      name: 'LM Studio (Local)',
      baseUrl: 'http://localhost:1234',
      suggestedDefaultModel: 'local-model',
      isLocal: true,
      headers: () => ({
        'Content-Type': 'application/json'
      }),
      listModels: async (key, baseUrl) => {
        const base = baseUrl || 'http://localhost:1234';
        const res = await fetch(`${base}/v1/models`);
        if (!res.ok) throw parseError('lmstudio', res.status, await res.text());
        const data = await res.json();
        return (data.data || []).map(m => m.id).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, baseUrl }) => {
        validateModel(model);
        const base = baseUrl || 'http://localhost:1234';
        const body = {
          model,
          messages: normalizeMessages('lmstudio', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        const res = await fetch(`${base}/v1/chat/completions`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body),
          signal
        });
        if (!res.ok) throw parseError('lmstudio', res.status, await res.text());
        return res;
      }
    },

    custom: {
      id: 'custom',
      name: 'Custom (OpenAI-Compatible)',
      baseUrl: '',
      suggestedDefaultModel: '',
      headers: (key) => {
        const h = { 'Content-Type': 'application/json' };
        if (key) h['Authorization'] = `Bearer ${key}`;
        return h;
      },
      listModels: async (key, baseUrl) => {
        if (!baseUrl) throw new Error('Base URL is required for custom OpenAI-compatible provider.');
        validateCustomUrl(baseUrl);
        const url = `${baseUrl.replace(/\/$/, '')}/models`;
        const res = await fetchDirectOrRelay('custom', url, {
          method: 'GET',
          headers: PROVIDERS.custom.headers(key)
        }, key);
        const data = await res.json();
        if (!res.ok) throw parseError('custom', res.status, data);
        const list = Array.isArray(data.data) ? data.data : (Array.isArray(data) ? data : []);
        return list.map(m => typeof m === 'string' ? m : m.id).filter(Boolean).sort();
      },
      chat: async ({ messages, model, stream, signal, maxTokens, key, baseUrl }) => {
        if (!baseUrl) throw new Error('Base URL is required for custom OpenAI-compatible provider.');
        validateCustomUrl(baseUrl);
        validateModel(model);
        const url = `${baseUrl.replace(/\/$/, '')}/chat/completions`;
        const body = {
          model,
          messages: normalizeMessages('custom', messages),
          stream: !!stream
        };
        if (maxTokens) body.max_tokens = maxTokens;
        return executeChatRequest('custom', url, PROVIDERS.custom.headers(key), body, stream, signal, key);
      }
    }
  };

  // --- Helper: Validate Model (Strict Part 1B rule: NEVER placeholder or custom-model) ---
  function validateModel(model) {
    if (!model || typeof model !== 'string') {
      const err = new Error('Choose a model first.');
      err.code = 'NO_MODEL_SELECTED';
      err.friendlyMessage = 'Please select a valid AI model from the model picker before sending requests.';
      throw err;
    }
    const clean = model.trim().toLowerCase();
    if (!clean || clean === 'custom-model' || clean === 'default' || clean === 'none' || clean === 'placeholder') {
      const err = new Error('Choose a model first.');
      err.code = 'INVALID_MODEL';
      err.friendlyMessage = 'A valid model must be selected. Tap "Pick Model" to choose from available models.';
      throw err;
    }
  }

  function validateCustomUrl(url) {
    if (!url.startsWith('https://') && !url.startsWith('http://localhost') && !url.startsWith('http://127.0.0.1')) {
      throw new Error('Custom provider URL must use HTTPS (or localhost for local testing).');
    }
  }

  function normalizeMessages(providerId, messages) {
    return messages.filter(m => m && m.content && m.content.trim()).map(m => ({
      role: m.role === 'system' ? 'system' : (m.role === 'assistant' ? 'assistant' : 'user'),
      content: m.content.trim()
    }));
  }

  // --- Sanitize API Keys (Part 1C) ---
  function sanitizeApiKey(rawKey) {
    if (!rawKey || typeof rawKey !== 'string') return '';
    let key = rawKey.trim().replace(/[\r\n\t]/g, '').trim();
    let prev;
    do {
      prev = key;
      key = key.replace(/^Bearer\s+/i, '').trim();
      key = key.replace(/^['"]+|['"]+$/g, '').trim();
    } while (key !== prev);
    return key;
  }

  function maskApiKey(key) {
    if (!key) return '';
    if (key.length <= 4) return '••••';
    const last4 = key.slice(-4);
    return '••••••••••••' + last4;
  }

  function detectProviderFromKey(key) {
    if (!key) return null;
    const clean = key.trim();
    if (clean.startsWith('sk-proj-') || (clean.startsWith('sk-') && !clean.startsWith('sk-ant-') && !clean.startsWith('sk-or-'))) {
      return 'openai';
    }
    if (clean.startsWith('gsk_')) return 'groq';
    if (clean.startsWith('sk-or-')) return 'openrouter';
    if (clean.startsWith('sk-ant-')) return 'anthropic';
    if (clean.startsWith('AIza')) return 'gemini';
    if (clean.startsWith('dsk-')) return 'deepseek';
    if (clean.startsWith('xai-')) return 'xai';
    if (clean.startsWith('fw_')) return 'fireworks';
    if (clean.startsWith('pplx-')) return 'perplexity';
    return null;
  }

  // --- Direct vs Relay Network Execution (Part 1E) ---
  async function fetchDirectOrRelay(providerId, url, options, key) {
    const provider = PROVIDERS[providerId];
    const settings = getSettings();
    const useRelay = settings.useRelay?.[providerId] || provider?.requiresRelayByDefault;

    if (!useRelay && !provider?.isLocal) {
      try {
        const directRes = await fetch(url, options);
        return directRes;
      } catch (err) {
        // Direct call failed (likely CORS or browser network block)
        // Check if TypeError (Failed to fetch)
        if (err.name === 'TypeError' || err.message.includes('fetch') || err.message.includes('NetworkError')) {
          // Fallback to relay and remember
          rememberUseRelay(providerId, true);
          return callRelay(url, options.method, options.headers, options.body);
        }
        throw err;
      }
    } else if (provider?.isLocal) {
      return fetch(url, options);
    } else {
      return callRelay(url, options.method, options.headers, options.body);
    }
  }

  async function callRelay(targetUrl, method, headers, body) {
    // Obtain client Firebase ID Token if user is logged in
    let authToken = '';
    try {
      if (typeof window !== 'undefined' && window.firebase && window.firebase.auth) {
        const user = window.firebase.auth().currentUser;
        if (user) authToken = await user.getIdToken();
      }
    } catch (_) {}

    const relayHeaders = {
      'Content-Type': 'application/json'
    };
    if (authToken) {
      relayHeaders['Authorization'] = `Bearer ${authToken}`;
    }

    const relayRes = await fetch('/api/ai/relay', {
      method: 'POST',
      headers: relayHeaders,
      body: JSON.stringify({
        url: targetUrl,
        method: method || 'POST',
        headers: headers || {},
        body: typeof body === 'string' ? JSON.parse(body) : body
      })
    });

    return relayRes;
  }

  function rememberUseRelay(providerId, enabled) {
    const s = getSettings();
    if (!s.useRelay) s.useRelay = {};
    s.useRelay[providerId] = !!enabled;
    saveSettings(s);
  }

  // --- Chat Execution with Auto-retry for Unsupported Params (Part 1D & 1F) ---
  async function executeChatRequest(providerId, url, headers, body, stream, signal, key) {
    const options = {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
      signal
    };

    let res = await fetchDirectOrRelay(providerId, url, options, key);

    // If 400 Bad Request mentioning unsupported parameter (e.g. max_tokens / temperature)
    if (res.status === 400) {
      const clone = res.clone();
      try {
        const errJson = await clone.json();
        const msg = JSON.stringify(errJson).toLowerCase();
        let retryNeeded = false;
        if ((msg.includes('max_tokens') || msg.includes('max_completion_tokens')) && (body.max_tokens || body.max_completion_tokens)) {
          delete body.max_tokens;
          delete body.max_completion_tokens;
          retryNeeded = true;
        }
        if (msg.includes('temperature') && body.temperature !== undefined) {
          delete body.temperature;
          retryNeeded = true;
        }
        if (retryNeeded) {
          options.body = JSON.stringify(body);
          res = await fetchDirectOrRelay(providerId, url, options, key);
        }
      } catch (_) {}
    }

    if (!res.ok) {
      let errPayload;
      try {
        errPayload = await res.json();
      } catch (_) {
        errPayload = await res.text();
      }
      throw parseError(providerId, res.status, errPayload);
    }

    return res;
  }

  // --- Error Parser (Part 1F: Human-readable, no raw JSON, collapsible details) ---
  function parseError(providerId, status, raw) {
    let rawStr = typeof raw === 'object' ? JSON.stringify(raw) : String(raw);
    let message = '';
    let errorCode = '';

    if (typeof raw === 'object' && raw !== null) {
      if (raw.error && typeof raw.error === 'object') {
        message = raw.error.message || '';
        errorCode = raw.error.code || raw.error.type || '';
      } else if (typeof raw.error === 'string') {
        message = raw.error;
      } else if (raw.message) {
        message = raw.message;
      }
    }

    // Strip any accidental API keys from details string
    const sanitizedDetails = rawStr.replace(/sk-[a-zA-Z0-9_\-]{8,}/g, 'sk-***')
      .replace(/gsk_[a-zA-Z0-9_\-]{8,}/g, 'gsk_***')
      .replace(/AIza[a-zA-Z0-9_\-]{8,}/g, 'AIza***');

    const providerName = PROVIDERS[providerId]?.name || providerId;
    let friendlyMessage = '';
    let isModelNotFoundError = false;

    if (status === 401) {
      friendlyMessage = `The provider rejected this key. Check that it is for ${providerName} and hasn't been revoked.`;
    } else if (status === 403) {
      friendlyMessage = `Access forbidden by ${providerName}. Verify billing is active, or that this key has permission for the selected model.`;
    } else if (status === 404 || errorCode === 'model_not_found' || message.includes('does not exist')) {
      isModelNotFoundError = true;
      friendlyMessage = `That model isn't available for this key. Tap "Change Model" to select an available model.`;
    } else if (status === 408) {
      friendlyMessage = `Request to ${providerName} timed out. Please try again.`;
    } else if (status === 413) {
      friendlyMessage = `The context or prompt is too large for ${providerName}. Shorten the code or selection.`;
    } else if (status === 429) {
      friendlyMessage = `Rate limit or quota reached on ${providerName}. Wait a moment before retrying, or check your account credits.`;
    } else if (status >= 500) {
      friendlyMessage = `${providerName} is currently experiencing server problems. Please retry shortly.`;
    } else if (status === 400) {
      friendlyMessage = message ? `Invalid request: ${message}` : `The request parameters were rejected by ${providerName}.`;
    } else {
      friendlyMessage = message || `Failed to communicate with ${providerName} (HTTP ${status}).`;
    }

    const err = new Error(friendlyMessage);
    err.status = status;
    err.providerId = providerId;
    err.errorCode = errorCode;
    err.friendlyMessage = friendlyMessage;
    err.isModelNotFound = isModelNotFoundError;
    err.details = sanitizedDetails;
    return err;
  }

  // --- Unified Stream Parser (Part 1G: OpenAI SSE, Anthropic events, Gemini SSE) ---
  async function* parseStream(providerId, response) {
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    try {
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // Keep partial line

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed || trimmed.startsWith(':')) continue; // Skip SSE keep-alive comments

          if (providerId === 'anthropic') {
            if (trimmed.startsWith('data:')) {
              const jsonStr = trimmed.slice(5).trim();
              if (jsonStr === '[DONE]') return;
              try {
                const data = JSON.parse(jsonStr);
                if (data.type === 'content_block_delta' && data.delta && data.delta.text) {
                  yield data.delta.text;
                }
              } catch (_) {}
            }
          } else if (providerId === 'gemini') {
            if (trimmed.startsWith('data:')) {
              const jsonStr = trimmed.slice(5).trim();
              try {
                const data = JSON.parse(jsonStr);
                const candidates = data.candidates || [];
                for (const c of candidates) {
                  const parts = c.content?.parts || [];
                  for (const p of parts) {
                    if (p.text) yield p.text;
                  }
                }
              } catch (_) {}
            }
          } else if (providerId === 'ollama') {
            try {
              const data = JSON.parse(trimmed);
              if (data.message && data.message.content) {
                yield data.message.content;
              }
            } catch (_) {}
          } else {
            // Standard OpenAI-compatible format
            if (trimmed.startsWith('data:')) {
              const jsonStr = trimmed.slice(5).trim();
              if (jsonStr === '[DONE]') return;
              try {
                const data = JSON.parse(jsonStr);
                const text = data.choices?.[0]?.delta?.content || '';
                if (text) yield text;
              } catch (_) {}
            }
          }
        }
      }
    } finally {
      reader.releaseLock();
    }
  }

  // --- Local Device Storage Management (Part 1H: Device-only, Never Synced) ---
  function getStoredKeys() {
    if (typeof localStorage === 'undefined') return {};
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.KEYS);
      return raw ? JSON.parse(raw) : {};
    } catch (_) {
      return {};
    }
  }

  function setStoredKey(providerId, key) {
    if (typeof localStorage === 'undefined') return;
    const sanitized = sanitizeApiKey(key);
    const keys = getStoredKeys();
    if (!sanitized) {
      delete keys[providerId];
      // Also clear cached models when key is removed
      clearModelsCache(providerId);
    } else {
      keys[providerId] = sanitized;
    }
    localStorage.setItem(STORAGE_KEYS.KEYS, JSON.stringify(keys));
  }

  function getSettings() {
    if (typeof localStorage === 'undefined') {
      return { activeProvider: 'openai', models: {}, useRelay: {}, customUrls: {} };
    }
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.SETTINGS);
      const s = raw ? JSON.parse(raw) : {};
      if (!s.activeProvider) s.activeProvider = 'openai';
      if (!s.models) s.models = {};
      if (!s.useRelay) s.useRelay = {};
      if (!s.customUrls) s.customUrls = {};

      // Cleanup stale / placeholder models (Part 1B: treat empty or "custom-model" as unset)
      Object.keys(s.models).forEach(p => {
        const m = (s.models[p] || '').trim().toLowerCase();
        if (!m || m === 'custom-model' || m === 'default' || m === 'placeholder') {
          delete s.models[p];
        }
      });

      return s;
    } catch (_) {
      return { activeProvider: 'openai', models: {}, useRelay: {}, customUrls: {} };
    }
  }

  function saveSettings(s) {
    if (typeof localStorage === 'undefined') return;
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(s));
  }

  // --- Model Discovery & Cache (Part 1B) ---
  function getCachedModels(providerId) {
    if (typeof localStorage === 'undefined') return null;
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.MODELS_CACHE_PREFIX + providerId);
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      if (Date.now() - (parsed.cachedAt || 0) < CACHE_DURATION_MS) {
        return parsed.models || [];
      }
    } catch (_) {}
    return null;
  }

  function setCachedModels(providerId, models) {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(
        STORAGE_KEYS.MODELS_CACHE_PREFIX + providerId,
        JSON.stringify({ cachedAt: Date.now(), models })
      );
    } catch (_) {}
  }

  function clearModelsCache(providerId) {
    if (typeof localStorage === 'undefined') return;
    localStorage.removeItem(STORAGE_KEYS.MODELS_CACHE_PREFIX + providerId);
  }

  async function discoverModels(providerId, forceRefresh = false) {
    const provider = PROVIDERS[providerId];
    if (!provider) throw new Error(`Unknown provider: ${providerId}`);

    if (!forceRefresh) {
      const cached = getCachedModels(providerId);
      if (cached && cached.length > 0) return cached;
    }

    const keys = getStoredKeys();
    const key = keys[providerId] || '';
    const settings = getSettings();
    const baseUrl = settings.customUrls?.[providerId] || provider.baseUrl;

    if (!key && !provider.isLocal) {
      throw new Error(`Please enter an API key for ${provider.name} first.`);
    }

    const models = await provider.listModels(key, baseUrl);
    if (Array.isArray(models) && models.length > 0) {
      setCachedModels(providerId, models);
      // Auto-preselect sensible model if none chosen yet
      if (!settings.models[providerId]) {
        let best = models.find(m => m === provider.suggestedDefaultModel) ||
                   models.find(m => m.includes('mini') || m.includes('flash') || m.includes('chat') || m.includes('instruct')) ||
                   models[0];
        if (best) {
          settings.models[providerId] = best;
          saveSettings(settings);
        }
      }
    }
    return models;
  }

  // --- Test API Key (Part 1C: Two-step verification) ---
  async function testProvider(providerId) {
    const provider = PROVIDERS[providerId];
    if (!provider) throw new Error(`Unknown provider: ${providerId}`);

    const keys = getStoredKeys();
    const key = keys[providerId] || '';
    const settings = getSettings();
    const baseUrl = settings.customUrls?.[providerId] || provider.baseUrl;

    if (!key && !provider.isLocal) {
      return {
        ok: false,
        step: 'key',
        error: `No API key entered for ${provider.name}.`
      };
    }

    // Step 1: Discover models
    let models = [];
    try {
      models = await discoverModels(providerId, true);
    } catch (err) {
      return {
        ok: false,
        step: 'listModels',
        error: err.friendlyMessage || err.message,
        details: err.details || ''
      };
    }

    // Step 2: Tiny chat test if model is available
    const chosenModel = settings.models[providerId] || provider.suggestedDefaultModel || models[0];
    let chatOk = false;
    let chatErr = null;

    if (chosenModel) {
      try {
        const testRes = await provider.chat({
          messages: [{ role: 'user', content: 'Reply with OK' }],
          model: chosenModel,
          stream: false,
          maxTokens: 5,
          key,
          baseUrl
        });
        chatOk = testRes.ok;
      } catch (err) {
        chatErr = err;
      }
    }

    return {
      ok: true,
      modelsCount: models.length,
      models,
      modelTested: chosenModel,
      chatOk,
      chatError: chatErr ? (chatErr.friendlyMessage || chatErr.message) : null
    };
  }

  return {
    PROVIDERS,
    KNOWN_AI_HOSTS,
    getStoredKeys,
    setStoredKey,
    sanitizeApiKey,
    maskApiKey,
    detectProviderFromKey,
    getSettings,
    saveSettings,
    discoverModels,
    testProvider,
    parseError,
    parseStream,
    validateModel
  };
}));
