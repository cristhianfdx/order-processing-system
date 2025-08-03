package jsondatabase

import (
	"go-api/internal/domain/model"
	"go-api/internal/domain/ports"
	"go-api/internal/shared/utils"
)

type JSONCustomerRepository struct {
	filePath string
}

func NewJSONCustomerRepository(filePath string) ports.CustomerRepository {
	return &JSONCustomerRepository{filePath: filePath}
}

func (r *JSONCustomerRepository) GetByID(id string) (*model.Customer, error) {
	customer, err := utils.FindByIDFromJSON[model.Customer](r.filePath, id)
	if err != nil {
		return nil, err
	}

	return customer, nil
}
