import { useState, useRef, useEffect } from 'react';
import { useSymbolSearch } from '@/hooks/useMarketData';
import type { SymbolSearchResult } from '@/services/marketData';

interface SymbolSearchProps {
  onSelectSymbol: (symbol: SymbolSearchResult) => void;
  placeholder?: string;
  className?: string;
}

export function SymbolSearch({ onSelectSymbol, placeholder = "Search stocks (e.g., TSLA, AAPL)...", className = "" }: SymbolSearchProps) {
  const [keyword, setKeyword] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const searchRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: results = [], isLoading } = useSymbolSearch(keyword);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleInputChange = (value: string) => {
    setKeyword(value);
    setIsOpen(true);
    setSelectedIndex(-1);
  };

  const handleSelect = (result: SymbolSearchResult) => {
    onSelectSymbol(result);
    setKeyword('');
    setIsOpen(false);
    setSelectedIndex(-1);
    inputRef.current?.blur();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!isOpen || results.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex(prev => (prev < results.length - 1 ? prev + 1 : prev));
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex(prev => (prev > 0 ? prev - 1 : -1));
        break;
      case 'Enter':
        e.preventDefault();
        if (selectedIndex >= 0 && selectedIndex < results.length) {
          handleSelect(results[selectedIndex]);
        }
        break;
      case 'Escape':
        setIsOpen(false);
        setSelectedIndex(-1);
        break;
    }
  };

  const containerStyle = {
    position: 'relative' as const,
    width: '100%',
  };

  const inputStyle = {
    width: '100%',
    padding: '12px 16px',
    fontSize: '14px',
    fontWeight: 500,
    color: '#e2e8f0',
    backgroundColor: '#0F1419',
    border: '1px solid #1e293b',
    borderRadius: '8px',
    outline: 'none',
    transition: 'all 0.2s',
  };

  const inputFocusStyle = {
    borderColor: '#0ea5e9',
    boxShadow: '0 0 0 3px rgba(14, 165, 233, 0.1)',
  };

  const dropdownStyle = {
    position: 'absolute' as const,
    top: 'calc(100% + 8px)',
    left: 0,
    right: 0,
    backgroundColor: '#0F1419',
    border: '1px solid #1e293b',
    borderRadius: '8px',
    maxHeight: '400px',
    overflowY: 'auto' as const,
    zIndex: 1000,
    boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.3)',
  };

  const loadingStyle = {
    padding: '16px',
    textAlign: 'center' as const,
    color: '#64748b',
    fontSize: '14px',
  };

  const emptyStyle = {
    padding: '16px',
    textAlign: 'center' as const,
    color: '#64748b',
    fontSize: '14px',
  };

  const resultItemStyle = {
    padding: '12px 16px',
    cursor: 'pointer',
    transition: 'background-color 0.15s',
    borderBottom: '1px solid #1e293b',
  };

  const resultItemSelectedStyle = {
    backgroundColor: '#0ea5e9',
  };

  const symbolStyle = {
    fontSize: '14px',
    fontWeight: 600,
    color: '#0ea5e9',
    marginBottom: '4px',
  };

  const nameStyle = {
    fontSize: '13px',
    color: '#e2e8f0',
    marginBottom: '4px',
  };

  const metaStyle = {
    fontSize: '12px',
    color: '#64748b',
    display: 'flex',
    gap: '8px',
    flexWrap: 'wrap' as const,
  };

  const badgeStyle = {
    padding: '2px 6px',
    borderRadius: '4px',
    fontSize: '11px',
    fontWeight: 500,
  };

  const typeBadgeStyle = {
    ...badgeStyle,
    backgroundColor: '#1e293b',
    color: '#94a3b8',
  };

  const regionBadgeStyle = {
    ...badgeStyle,
    backgroundColor: 'rgba(14, 165, 233, 0.1)',
    color: '#0ea5e9',
  };

  return (
    <div ref={searchRef} style={containerStyle} className={className}>
      <input
        ref={inputRef}
        type="text"
        value={keyword}
        onChange={(e) => handleInputChange(e.target.value)}
        onFocus={() => keyword && setIsOpen(true)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        style={inputStyle}
        onMouseEnter={(e) => {
          e.currentTarget.style.borderColor = '#0ea5e9';
        }}
        onMouseLeave={(e) => {
          if (document.activeElement !== e.currentTarget) {
            e.currentTarget.style.borderColor = '#1e293b';
          }
        }}
        onFocusCapture={(e) => {
          Object.assign(e.currentTarget.style, inputFocusStyle);
        }}
        onBlurCapture={(e) => {
          e.currentTarget.style.borderColor = '#1e293b';
          e.currentTarget.style.boxShadow = 'none';
        }}
      />

      {isOpen && keyword && (
        <div style={dropdownStyle}>
          {isLoading && (
            <div style={loadingStyle}>
              Searching for "{keyword}"...
            </div>
          )}

          {!isLoading && results.length === 0 && (
            <div style={emptyStyle}>
              No results found for "{keyword}"
            </div>
          )}

          {!isLoading && results.length > 0 && (
            <>
              {results.map((result, index) => (
                <div
                  key={result.symbol}
                  style={{
                    ...resultItemStyle,
                    ...(selectedIndex === index ? resultItemSelectedStyle : {}),
                  }}
                  onClick={() => handleSelect(result)}
                  onMouseEnter={(e) => {
                    if (selectedIndex !== index) {
                      e.currentTarget.style.backgroundColor = '#1e293b';
                    }
                    setSelectedIndex(index);
                  }}
                  onMouseLeave={(e) => {
                    if (selectedIndex !== index) {
                      e.currentTarget.style.backgroundColor = 'transparent';
                    }
                  }}
                >
                  <div style={symbolStyle}>{result.symbol}</div>
                  <div style={nameStyle}>{result.name}</div>
                  <div style={metaStyle}>
                    <span style={typeBadgeStyle}>{result.type}</span>
                    <span style={regionBadgeStyle}>{result.region}</span>
                    {result.currency && (
                      <span style={{ color: '#64748b' }}>• {result.currency}</span>
                    )}
                  </div>
                </div>
              ))}
            </>
          )}
        </div>
      )}
    </div>
  );
}
