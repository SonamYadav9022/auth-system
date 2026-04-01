import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import type { LoginRequest, AuthResponse } from "../types/auth";

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginRequest>();
  const { login } = useAuth();
  const navigate = useNavigate();

  const mutation = useMutation({
  mutationFn: (data: LoginRequest) =>
    api.post<AuthResponse>("/api/auth/login", data).then((r) => r.data),

  onSuccess: (data) => {
    login(data);
    navigate("/dashboard");
  },
});

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Welcome Back</h2>

        {mutation.isError && (
          <div className="bg-red-100 text-red-600 p-3 rounded mb-4 text-sm">
            Invalid email or password.
          </div>
        )}

        <form onSubmit={handleSubmit((data) => mutation.mutate(data))} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input {...register("email", { required: "Email is required" })}
              type="email"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="john@example.com" />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input {...register("password", { required: "Password is required" })}
              type="password"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="••••••••" />
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
          </div>

          <button type="submit" disabled={mutation.isPending}
            className="w-full bg-blue-600 text-white py-2 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 transition">
            {mutation.isPending ? "Logging in..." : "Login"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-4">
          Don't have an account?{" "}
          <Link to="/register" className="text-blue-600 hover:underline font-medium">Register</Link>
        </p>
      </div>
    </div>
  );
}