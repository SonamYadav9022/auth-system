import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";


export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const publicQuery = useQuery({
  queryKey: ["public"],
  queryFn: () => api.get("/api/public").then(r => r.data),
  refetchOnMount: true,
  
});

  const userQuery = useQuery({
  queryKey: ["user"],
  queryFn: () => api.get("/api/user").then(r => r.data),
  enabled: !!user,
  refetchOnMount: true,
});

  const adminQuery = useQuery({
  queryKey: ["admin"],
  queryFn: () => api.get("/api/admin").then(r => r.data),
  enabled: user?.role === "ADMIN",
  retry: false,
  refetchOnMount: true,
});

useEffect(() => {
  publicQuery.refetch();
  userQuery.refetch();
  adminQuery.refetch();
}, []);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navbar */}
      <nav className="bg-white shadow px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-600">AuthSystem</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-600 text-sm">
            👤 <strong>{user?.name}</strong>
            <span className={`ml-2 px-2 py-0.5 rounded-full text-xs font-semibold ${
              user?.role === "ADMIN" ? "bg-purple-100 text-purple-700" : "bg-blue-100 text-blue-700"
            }`}>{user?.role}</span>
          </span>
          <button onClick={handleLogout}
            className="bg-red-500 text-white px-4 py-1.5 rounded-lg text-sm hover:bg-red-600 transition">
            Logout
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto p-6 space-y-6">
        <h2 className="text-2xl font-bold text-gray-800">Dashboard</h2>

        {/* Public Card */}
        <div className="bg-white rounded-xl shadow p-6 border-l-4 border-green-500">
          <h3 className="text-lg font-semibold text-green-700 mb-2">🌐 Public Content</h3>
          <p className="text-gray-600">
            {publicQuery.isLoading ? "Loading..." : publicQuery.data?.message}
          </p>
        </div>

        {/* User Card */}
        <div className="bg-white rounded-xl shadow p-6 border-l-4 border-blue-500">
          <h3 className="text-lg font-semibold text-blue-700 mb-2">👤 User Content</h3>
          <p className="text-gray-600">
            {userQuery.isLoading ? "Loading..." : userQuery.data?.message}
          </p>
        </div>

        {/* Admin Card - only shown for ADMIN */}
        {user?.role === "ADMIN" && (
          <div className="bg-white rounded-xl shadow p-6 border-l-4 border-purple-500">
            <h3 className="text-lg font-semibold text-purple-700 mb-2">🔐 Admin Content</h3>
            <p className="text-gray-600">
              {adminQuery.isLoading ? "Loading..." : adminQuery.data?.message}
            </p>
          </div>
        )}

        {user?.role === "USER" && (
          <div className="bg-gray-50 rounded-xl border border-dashed border-gray-300 p-6 text-center text-gray-400">
            🔒 Admin content is restricted to administrators only.
          </div>
        )}
      </div>
    </div>
  );
}