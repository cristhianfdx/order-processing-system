package ports

import "go-api/internal/domain/model"

type ProductRepository interface {
	GetByID(id string) (*model.Product, error)
}
