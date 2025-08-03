package jsondatabase

import (
	"go-api/internal/domain/model"
	"go-api/internal/domain/ports"
	"go-api/internal/shared/utils"
)

type JSONProductRepository struct {
	filePath string
}

func NewJSONProductRepository(filePath string) ports.ProductRepository {
	return &JSONProductRepository{filePath: filePath}
}

func (r *JSONProductRepository) GetByID(id string) (*model.Product, error) {
	product, err := utils.FindByIDFromJSON[model.Product](r.filePath, id)
	if err != nil {
		return nil, err
	}

	return product, nil
}
