package handler

import (
	services "go-api/internal/application/services"
	"go-api/internal/domain/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

type CustomerHandler struct {
	service *services.CustomerService
}

func NewCustomerHandler(s *services.CustomerService) *CustomerHandler {
	return &CustomerHandler{service: s}
}

// GetCustomer godoc
// @Summary Get Customer by ID
// @Description Get Customer data by Customer ID
// @Tags Customers
// @Accept  json
// @Produce  json
// @Param   id   path    string  true  "Customer ID"
// @Success 200  {object} model.Customer
// @Failure 404  {object} model.ErrorResponse
// @Router /api/customers/{id} [get]
func (h *CustomerHandler) GetCustomer(c *gin.Context) {
	id := c.Param("id")
	customer, err := h.service.GetCustomer(id)
	if err != nil {
		c.JSON(http.StatusNotFound, model.ErrorResponse{Message: "not found"})
	}

	c.JSON(http.StatusOK, customer)
}
