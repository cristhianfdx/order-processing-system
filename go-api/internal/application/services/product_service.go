package services

import (
	"go-api/internal/domain/model"
	port "go-api/internal/domain/ports"
)

type ProductService struct {
	repo port.ProductRepository
}

func NewProductService(repo port.ProductRepository) *ProductService {
	return &ProductService{repo: repo}
}

func (ps *ProductService) GetProduct(id string) (*model.Product, error) {
	return ps.repo.GetByID(id)
}
