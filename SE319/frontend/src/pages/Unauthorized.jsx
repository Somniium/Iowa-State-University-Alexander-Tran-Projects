import { Link } from "react-router-dom";

export default function Unauthorized() {
  const userStr = localStorage.getItem("user");
  const user = userStr ? JSON.parse(userStr) : null;

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="max-w-md w-full text-center">
        <div className="text-6xl mb-4">🔐</div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Access Denied
        </h1>
        <p className="text-gray-600 mb-6">
          You don't have permission to access this page.
          {user && (
            <span className="block mt-2">
              Your role: <span className="font-semibold">{user.role}</span>
            </span>
          )}
        </p>
        <div className="space-y-3">
          <Link
            to="/"
            className="inline-block w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
          >
            Go to Home
          </Link>
        </div>
      </div>
    </div>
  );
}