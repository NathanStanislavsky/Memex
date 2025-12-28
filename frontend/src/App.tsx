import { useState, useEffect } from "react";
import axios from "axios";
import "./App.css";
import { apiUrl } from "./config";

interface MemexDocument {
  id: string;
  content: string;
}

function App() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<MemexDocument[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState("");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [activeNav, setActiveNav] = useState("all");

  // Command K shortcut
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        const searchInput = document.querySelector(".search-bar") as HTMLInputElement;
        searchInput?.focus();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      return;
    }

    const timeoutId = setTimeout(async () => {
      setLoading(true);
      try {
        const response = await axios.get(
          `${apiUrl('api/search')}?q=${encodeURIComponent(query)}`
        );
        setResults(response.data);
      } catch (error) {
        console.error("Search failed: ", error);
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [query]);


  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    try {
      const formData = new FormData();
      for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
      }
      
      await axios.post(
        apiUrl('api/upload'),
        formData
      );
      const fileCount = files.length;
      setUploadStatus(`${fileCount} file${fileCount > 1 ? 's' : ''} uploaded successfully`);
      setTimeout(() => setUploadStatus(""), 3000);
    } catch (error) {
      console.error("Upload failed: ", error);
      setUploadStatus("Upload failed. Please try again.");
    }
  };

  const highlightText = (text: string, searchQuery: string): React.ReactElement => {
    if (!searchQuery.trim()) {
      return <>{text}</>;
    }

    const parts = text.split(new RegExp(`(${searchQuery})`, "gi"));
    return (
      <>
        {parts.map((part, index) =>
          part.toLowerCase() === searchQuery.toLowerCase() ? (
            <span key={index} className="highlight">
              {part}
            </span>
          ) : (
            part
          )
        )}
      </>
    );
  };

  const truncateText = (text: string, maxLength: number = 200): string => {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + "...";
  };

  return (
    <div className="app-container">
      {/* Mobile Sidebar Toggle */}
      <button
        className="mobile-sidebar-toggle"
        onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
        aria-label="Toggle sidebar"
      >
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M3 5h14M3 10h14M3 15h14" />
        </svg>
      </button>

      {/* Sidebar */}
      <aside className={`sidebar ${sidebarCollapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <h1 className="sidebar-title">Memex</h1>
          <button
            className="sidebar-toggle"
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            aria-label="Collapse sidebar"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M11 4L5 8l6 4" />
            </svg>
          </button>
        </div>
        <nav className="sidebar-nav">
          <div className="nav-section">
            <div className="nav-section-title">Navigation</div>
            <div
              className={`nav-item ${activeNav === "all" ? "active" : ""}`}
              onClick={() => setActiveNav("all")}
            >
              All Notes
            </div>
          </div>
        </nav>
      </aside>

      {/* Main Content */}
      <main className={`main-content ${sidebarCollapsed ? "sidebar-collapsed" : ""}`}>
        <div className="content-header">
          <div className="search-container">
            <input
              type="text"
              className="search-bar"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search your memories..."
              autoFocus
            />
            <div className="search-hint">
              <span>Press</span>
              <kbd>⌘</kbd>
              <kbd>K</kbd>
              <span>to search</span>
            </div>
          </div>
        </div>

        <div className="content-area">
          <div className="upload-section">
            <h3>Add to Memory</h3>
            <label className="upload-input-wrapper">
              <input
                type="file"
                className="upload-input"
                onChange={handleUpload}
                accept=".txt,.md,.pdf,.doc,.docx"
                multiple
              />
              <span className="upload-button">Choose Files</span>
            </label>
            {uploadStatus && (
              <div className="upload-status">{uploadStatus}</div>
            )}
          </div>

          <div className="results-container">
            {loading && (
              <div className="loading-state">Searching...</div>
            )}

            {!loading && results.length > 0 && (
              <div className="results-grid">
                {results.map((doc) => (
                  <div key={doc.id} className="knowledge-card">
                    <div className="card-header">
                      <span className="card-icon">📄</span>
                      <span className="card-id">{doc.id}</span>
                    </div>
                    <div className="card-content">
                      {highlightText(truncateText(doc.content), query)}
                    </div>
                  </div>
                ))}
              </div>
            )}

            {!loading && results.length === 0 && query && (
              <div className="empty-state">
                <div className="empty-state-icon">🔍</div>
                <div className="empty-state-text">No memories found for "{query}"</div>
              </div>
            )}

          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
