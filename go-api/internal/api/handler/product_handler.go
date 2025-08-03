package handler

import (
	services "go-api/internal/application/services"
	"go-api/internal/domain/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

type ProductHandler struct {
	service *services.ProductService
}

func NewProductHandler(s *services.ProductService) *ProductHandler {
	return &ProductHandler{service: s}
}

// GetProduct godoc
// @Summary Get Produdt by ID
// @Description Get Product data by ID
// @Tags Products
// @Accept  json
// @Produce  json
// @Param   id   path    string  true  "Product ID"
// @Success 200  {object} model.Product
// @Failure 404  {object} model.ErrorResponse
// @Router /api/products/{id} [get]
func (h *ProductHandler) GetProduct(c *gin.Context) {
	id := c.Param("id")
	product, err := h.service.GetProduct(id)
	if err != nil {
		c.JSON(http.StatusNotFound, model.ErrorResponse{Message: "not found"})
	}

	c.JSON(http.StatusOK, product)
}
