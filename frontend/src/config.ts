/**
 * API Configuration
 * 
 * The API base URL can be configured via environment variables:
 * 
 * For local development (backend runs on port 8081):
 *   Create a .env.local file with: VITE_API_BASE_URL=http://localhost:8081
 * 
 * For Docker setup (backend runs on port 8080):
 *   Create a .env.local file with: VITE_API_BASE_URL=http://localhost:8080
 * 
 * If no environment variable is set, it defaults to http://localhost:8081
 * 
 * Note: Vite requires environment variables to be prefixed with VITE_ to be exposed to the client
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

/**
 * Helper function to build complete API endpoint URLs
 * @param endpoint - The API endpoint path (e.g., 'api/search' or '/api/upload')
 * @returns Complete URL with base URL and endpoint
 */
export const apiUrl = (endpoint: string): string => {
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
  return `${API_BASE_URL}/${cleanEndpoint}`;
};

