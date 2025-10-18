import axios, { AxiosError } from 'axios';
import type {
  PortfolioResponse,
  CreatePortfolioRequest,
  AddOrUpdatePositionRequest,
  UpdatePortfolioOwnerRequest,
  RiskAssessmentResponse,
  ApiError,
} from '@/types/api';

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Error handler
export const handleApiError = (error: unknown): never => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    if (axiosError.response?.data) {
      throw axiosError.response.data;
    }
    throw new Error(axiosError.message || 'An unexpected error occurred');
  }
  throw error;
};

// Portfolio API
export const portfolioApi = {
  // Get all portfolios
  async getAll(): Promise<PortfolioResponse[]> {
    try {
      const response = await apiClient.get<PortfolioResponse[]>('/portfolios');
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Get portfolio by ID
  async getById(id: string): Promise<PortfolioResponse> {
    try {
      const response = await apiClient.get<PortfolioResponse>(`/portfolios/${id}`);
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Create new portfolio
  async create(data: CreatePortfolioRequest): Promise<PortfolioResponse> {
    try {
      const response = await apiClient.post<PortfolioResponse>('/portfolios', data);
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Update portfolio owner
  async updateOwner(
    id: string,
    data: UpdatePortfolioOwnerRequest
  ): Promise<PortfolioResponse> {
    try {
      const response = await apiClient.put<PortfolioResponse>(
        `/portfolios/${id}/owner`,
        data
      );
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Add or update position
  async addOrUpdatePosition(
    id: string,
    data: AddOrUpdatePositionRequest
  ): Promise<PortfolioResponse> {
    try {
      const response = await apiClient.put<PortfolioResponse>(
        `/portfolios/${id}/positions`,
        data
      );
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Remove position
  async removePosition(id: string, positionId: string): Promise<PortfolioResponse> {
    try {
      const response = await apiClient.delete<PortfolioResponse>(
        `/portfolios/${id}/positions/${positionId}`
      );
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Delete portfolio
  async delete(id: string): Promise<void> {
    try {
      await apiClient.delete(`/portfolios/${id}`);
    } catch (error) {
      return handleApiError(error);
    }
  },

  // Recalculate risk
  async recalculateRisk(id: string): Promise<RiskAssessmentResponse> {
    try {
      const response = await apiClient.post<RiskAssessmentResponse>(
        `/portfolios/${id}/recalculate-risk`
      );
      return response.data;
    } catch (error) {
      return handleApiError(error);
    }
  },
};

export default apiClient;
