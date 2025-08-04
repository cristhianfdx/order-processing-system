package services_test

import (
	"errors"
	"testing"

	"go-api/internal/application/services"
	"go-api/internal/domain/model"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

type MockProductRepository struct {
	mock.Mock
}

func (m *MockProductRepository) GetByID(id string) (*model.Product, error) {
	args := m.Called(id)
	if product, ok := args.Get(0).(*model.Product); ok {
		return product, args.Error(1)
	}
	return nil, args.Error(1)
}

func TestGetProduct_Success(t *testing.T) {
	mockRepo := new(MockProductRepository)
	service := services.NewProductService(mockRepo)

	expectedProduct := &model.Product{
		ID:    "123",
		Name:  "Test Product",
		Price: 10.5,
	}

	mockRepo.On("GetByID", "123").Return(expectedProduct, nil)

	product, err := service.GetProduct("123")

	assert.NoError(t, err)
	assert.Equal(t, expectedProduct, product)
	mockRepo.AssertExpectations(t)
}

func TestGetProduct_NotFound(t *testing.T) {
	mockRepo := new(MockProductRepository)
	service := services.NewProductService(mockRepo)

	mockRepo.On("GetByID", "999").Return((*model.Product)(nil), errors.New("not found"))

	product, err := service.GetProduct("999")

	assert.Nil(t, product)
	assert.EqualError(t, err, "not found")
	mockRepo.AssertExpectations(t)
}
