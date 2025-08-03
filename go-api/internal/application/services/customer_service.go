package services

import (
	"go-api/internal/domain/model"
	port "go-api/internal/domain/ports"
)

type CustomerService struct {
	repo port.CustomerRepository
}

func NewCustomerService(repo port.CustomerRepository) *CustomerService {
	return &CustomerService{repo: repo}
}

func (cs *CustomerService) GetCustomer(id string) (*model.Customer, error) {
	return cs.repo.GetByID(id)
}
