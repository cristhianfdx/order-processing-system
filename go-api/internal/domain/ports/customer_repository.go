package ports

import "go-api/internal/domain/model"

type CustomerRepository interface {
	GetByID(id string) (*model.Customer, error)
}
