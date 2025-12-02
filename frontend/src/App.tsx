import { useState } from "react";
import axios from "axios";
import "./App.css";

interface MemexDocument {
  id: string;
  content: string;
}

function App() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<MemexDocument[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState("");

  const handleSearch = async () => {
    if (!query) {
      return;
    }

    setLoading(true);

    try {
      const response = await axios.get(
        `http://localhost:8080/api/search?q=${query}`
      );
      setResults(response.data);
    } catch (error) {
      console.error("Search failed: ", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      const formData = new FormData();
      formData.append("file", file);
      
      const response = await axios.post(
        "http://localhost:8080/api/upload",
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        }
      );
      setUploadStatus(response.data);
    } catch (error) {
      console.error("Upload failed: ", error);
    }
  };

  return (
    <div style={{ maxWidth: "800px", margin: "0 auto", padding: "2rem" }}>
      <h1>Memex</h1>

      <div
        style={{
          marginBottom: "2rem",
          padding: "1rem",
          border: "1px solid #ccc",
        }}
      >
        <h3>Add to Memory</h3>
        <input type="file" onChange={handleUpload} />
        <p>{uploadStatus}</p>
      </div>

      <div style={{ display: "flex", gap: "10px", marginBottom: "1rem" }}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search your brain..."
          style={{ flex: 1, padding: "10px", fontSize: "16px" }}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        />
        <button onClick={handleSearch} style={{ padding: "10px 20px" }}>
          Search
        </button>
      </div>

      {loading && <p>Thinking...</p>}

      <div>
        {results.map((doc) => (
          <div
            key={doc.id}
            style={{
              marginBottom: "1rem",
              padding: "1rem",
              background: "#f5f5f5",
              borderRadius: "8px",
            }}
          >
            <h4 style={{ margin: "0 0 0.5rem 0" }}>📄 {doc.id}</h4>
            <p style={{ fontSize: "14px", color: "#555" }}>
              {doc.content.substring(0, 300)}...
            </p>
          </div>
        ))}
        {results.length === 0 && query && !loading && <p>No memories found.</p>}
      </div>
    </div>
  );
}

export default App;
