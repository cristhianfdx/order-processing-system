package services

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"

	"go-api/internal/domain/model"
)

type MockCustomerRepository struct {
	mock.Mock
}

func (m *MockCustomerRepository) GetByID(id string) (*model.Customer, error) {
	args := m.Called(id)
	customer := args.Get(0)
	if customer == nil {
		return nil, args.Error(1)
	}
	return customer.(*model.Customer), args.Error(1)
}

func TestCustomerService_GetCustomer(t *testing.T) {
	mockRepo := new(MockCustomerRepository)
	service := NewCustomerService(mockRepo)

	expectedCustomer := &model.Customer{
		ID:   "123",
		Name: "Alice",
	}

	mockRepo.
		On("GetByID", "123").
		Return(expectedCustomer, nil)

	result, err := service.GetCustomer("123")

	assert.NoError(t, err)
	assert.Equal(t, expectedCustomer, result)
	mockRepo.AssertExpectations(t)
}
